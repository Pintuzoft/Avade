/*
 * avade_uhm - user host-masking for bahamut, made to work with Avade.
 *
 * bahamut asks its modules what a host should be masked to (CHOOK_MASKHOST)
 * but has no masking of its own. This module answers, and Avade does the
 * very same calculation, so services always know the masked host of a user.
 *
 * The salt is never put on a server by hand. Services send it to all
 * servers with a module command:
 *
 *     :OperServ MODULE CGLOBAL avade_uhm SALT <salt> <prefix>
 *
 * and the module keeps it in <ircd dir>/avade_uhm.salt (mode 0600) so a
 * server that restarts while services are away still has it.
 *
 * The mask (type 1), h(x) being the first 8 hex digits of HMAC-SHA256(salt, x):
 *
 *     IPv4 a.b.c.d      h("a.b.c.d").h("a.b.c").h("a.b").ip
 *     IPv6              h(32 hex).h(first 16 hex).h(first 12 hex).ip6
 *     host with at least three labels, x.y.z.tld
 *                       <prefix>-h("x.y.z.tld").y.z.tld
 *     anything else     as the ip
 *
 * Bans can be set on the parts: *!*@*.<h(a.b.c)>.<h(a.b)>.ip is the /24.
 *
 * Build and install: see build.sh and README.md in this directory.
 */

#define BIRCMODULE

#include "struct.h"
#include "common.h"
#include "sys.h"
#include "h.h"
#include "hooks.h"
#include "send.h"

#include <sys/stat.h>
#include <fcntl.h>
#include <arpa/inet.h>
#include <openssl/evp.h>
#include <openssl/hmac.h>

#define UHM_VERSION     "1.0"
#define UHM_TYPE        1
#define UHM_HEXLEN      8
#define UHM_SALTMAX     128
#define UHM_PREFIXMAX   16
#define UHM_FILE        "avade_uhm.salt"

static char uhm_salt[UHM_SALTMAX + 1];
static char uhm_prefix[UHM_PREFIXMAX + 1] = "user";
static int  uhm_from_services = 0;      /* 0: a random salt until services tell us */

/* First UHM_HEXLEN hex digits of HMAC-SHA256(salt, data) */
static void
uhm_hash(const char *data, char *out)
{
    unsigned char md[EVP_MAX_MD_SIZE];
    unsigned int  len = 0;
    int           i;

    HMAC(EVP_sha256(), uhm_salt, strlen(uhm_salt),
         (const unsigned char *) data, strlen(data), md, &len);
    for(i = 0; i < UHM_HEXLEN / 2; i++)
        sprintf(out + i * 2, "%02x", md[i]);
    out[UHM_HEXLEN] = '\0';
}

static int
uhm_valid(const char *s, int max)
{
    int len = 0;

    for(; *s; s++, len++)
        if(!isalnum((unsigned char) *s))
            return 0;
    return (len > 0 && len <= max);
}

static void
uhm_save(void)
{
    char path[PATH_MAX + 32];
    int  fd;

    ircsnprintf(path, sizeof(path), "%s/%s", dpath, UHM_FILE);
    if((fd = open(path, O_WRONLY | O_CREAT | O_TRUNC, 0600)) < 0)
        return;
    if(write(fd, uhm_salt, strlen(uhm_salt)) < 0 || write(fd, " ", 1) < 0 ||
       write(fd, uhm_prefix, strlen(uhm_prefix)) < 0 || write(fd, "\n", 1) < 0)
        sendto_realops("avade_uhm: could not write %s", path);
    close(fd);
}

static int
uhm_load(void)
{
    char  path[PATH_MAX + 32], line[UHM_SALTMAX + UHM_PREFIXMAX + 8], *p;
    FILE *fp;

    ircsnprintf(path, sizeof(path), "%s/%s", dpath, UHM_FILE);
    if(!(fp = fopen(path, "r")))
        return 0;
    if(!fgets(line, sizeof(line), fp))
    {
        fclose(fp);
        return 0;
    }
    fclose(fp);
    line[strcspn(line, "\r\n")] = '\0';
    if((p = strchr(line, ' ')))
        *p++ = '\0';
    if(!uhm_valid(line, UHM_SALTMAX))
        return 0;
    strcpy(uhm_salt, line);
    if(p && uhm_valid(p, UHM_PREFIXMAX))
        strcpy(uhm_prefix, p);
    return 1;
}

/* Until services have told us the salt hosts are still masked, with a salt
 * nobody knows. The masks will not match what services calculate, but no
 * real host is shown in the meantime. */
static void
uhm_random_salt(void)
{
    unsigned char rnd[24];
    int           fd, i;

    memset(rnd, 0, sizeof(rnd));
    if((fd = open("/dev/urandom", O_RDONLY)) >= 0)
    {
        if(read(fd, rnd, sizeof(rnd)) < 0)
            rnd[0] = 1;
        close(fd);
    }
    for(i = 0; i < (int) sizeof(rnd); i++)
        sprintf(uhm_salt + i * 2, "%02x", rnd[i] ^ (unsigned char) (timeofday >> (i % 4) * 8));
}

static int
uhm_mask_ip(const char *ip, char *out)
{
    unsigned char a[16];
    char          buf[40], h1[UHM_HEXLEN + 1], h2[UHM_HEXLEN + 1], h3[UHM_HEXLEN + 1];
    int           i;

    if(inet_pton(AF_INET, ip, a) == 1)
    {
        ircsnprintf(buf, sizeof(buf), "%u.%u.%u.%u", a[0], a[1], a[2], a[3]);
        uhm_hash(buf, h1);
        ircsnprintf(buf, sizeof(buf), "%u.%u.%u", a[0], a[1], a[2]);
        uhm_hash(buf, h2);
        ircsnprintf(buf, sizeof(buf), "%u.%u", a[0], a[1]);
        uhm_hash(buf, h3);
        ircsnprintf(out, HOSTLEN + 1, "%s.%s.%s.ip", h1, h2, h3);
        return 1;
    }
    if(inet_pton(AF_INET6, ip, a) == 1)
    {
        for(i = 0; i < 16; i++)
            sprintf(buf + i * 2, "%02x", a[i]);
        uhm_hash(buf, h1);
        buf[16] = '\0';
        uhm_hash(buf, h2);
        buf[12] = '\0';
        uhm_hash(buf, h3);
        ircsnprintf(out, HOSTLEN + 1, "%s.%s.%s.ip6", h1, h2, h3);
        return 1;
    }
    return 0;
}

static int
uhm_mask_host(const char *host, char *out)
{
    char        lower[HOSTLEN + 1], h[UHM_HEXLEN + 1];
    const char *rest, *p;
    int         dots = 0, i;

    for(i = 0; host[i] && i < HOSTLEN; i++)
    {
        lower[i] = tolower((unsigned char) host[i]);
        if(lower[i] == '.')
            dots++;
        else if(lower[i] == ':')
            return 0;       /* an address, not a name */
    }
    lower[i] = '\0';
    if(dots < 2 || !(rest = strchr(lower, '.')))
        return 0;           /* the whole name would be left in the open */
    for(p = lower; *p; p++)
        if(!isdigit((unsigned char) *p) && *p != '.')
            break;
    if(!*p)
        return 0;           /* only digits and dots: an ip */

    uhm_hash(lower, h);
    if(strlen(uhm_prefix) + 1 + UHM_HEXLEN + strlen(rest) > HOSTLEN)
        return 0;
    ircsnprintf(out, HOSTLEN + 1, "%s-%s%s", uhm_prefix, h, rest);
    return 1;
}

/* CHOOK_MASKHOST: (char *orghost, char *orgip, char **newhost, int type),
 * where *newhost is the ircd's buffer of HOSTLEN + 1 to write the mask to */
static int
uhm_maskhost(char *orghost, char *orgip, char **newhost, int type)
{
    char *out = *newhost;

    if(type != UHM_TYPE)
        return UHM_SOFT_FAILURE;    /* another module's kind of masking */

    if(orghost && orgip && mycmp(orghost, orgip) && uhm_mask_host(orghost, out))
        return UHM_SUCCESS;
    if(orgip && uhm_mask_ip(orgip, out))
        return UHM_SUCCESS;
    if(orghost && uhm_mask_ip(orghost, out))
        return UHM_SUCCESS;

    /* Nothing we understand: never fall back to the real host */
    {
        char h[UHM_HEXLEN + 1];

        uhm_hash(orghost ? orghost : "", h);
        ircsnprintf(out, HOSTLEN + 1, "%s-%s.unknown", uhm_prefix, h);
    }
    return UHM_SUCCESS;
}

void
bircmodule_check(int *interface_version)
{
    *interface_version = MODULE_INTERFACE_VERSION;
}

int
bircmodule_init(void *self)
{
    if(uhm_load())
        uhm_from_services = 1;
    else
        uhm_random_salt();
    bircmodule_add_hook(CHOOK_MASKHOST, self, uhm_maskhost);
    return 0;
}

void
bircmodule_shutdown(void)
{
    memset(uhm_salt, 0, sizeof(uhm_salt));
}

void
bircmodule_getinfo(char **version, char **description)
{
    *version = UHM_VERSION;
    *description = "User host-masking that Avade IRC Services can follow";
}

/* /MODULE CMD avade_uhm [TEST <host> <ip>], server admins only (checked by the ircd) */
int
bircmodule_command(aClient *sptr, int parc, char **parv)
{
    if(parc > 3 && !mycmp(parv[1], "TEST"))
    {
        char out[HOSTLEN + 1], *p = out;

        uhm_maskhost(parv[2], parv[3], &p, UHM_TYPE);
        sendto_one(sptr, ":%s NOTICE %s :avade_uhm: %s (%s) -> %s", me.name,
                   sptr->name, parv[2], parv[3], out);
        return 0;
    }
    sendto_one(sptr, ":%s NOTICE %s :avade_uhm %s: salt %s, prefix %s", me.name,
               sptr->name, UHM_VERSION,
               uhm_from_services ? "set by services" : "not set yet (random until services link)",
               uhm_prefix);
    return 0;
}

/* :<services> MODULE CGLOBAL avade_uhm SALT <salt> [<prefix>]
 * parv[0] is the module name. The ircd has already passed the line on to
 * the other servers, and only lets servers and U-lined clients send it. */
int
bircmodule_globalcommand(aClient *cptr, aClient *sptr, int parc, char **parv)
{
    if(!IsULine(sptr))
        return 0;       /* the salt comes from services, nobody else */

    if(parc > 2 && !mycmp(parv[1], "SALT") && uhm_valid(parv[2], UHM_SALTMAX))
    {
        int changed = (!uhm_from_services || strcmp(uhm_salt, parv[2]));

        strcpy(uhm_salt, parv[2]);
        if(parc > 3 && uhm_valid(parv[3], UHM_PREFIXMAX))
        {
            if(strcmp(uhm_prefix, parv[3]))
                changed = 1;
            strcpy(uhm_prefix, parv[3]);
        }
        uhm_from_services = 1;
        if(changed)
        {
            uhm_save();
            sendto_realops("avade_uhm: host-masking salt set by %s", sptr->name);
        }
    }
    return 0;
}
