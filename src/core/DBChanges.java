/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.util.ArrayList;

/**
 *
 * @author Fredrik Karlsson aka DreamHealer & avade.net
 */
public class DBChanges extends HashNumeric {
    private Version version;
    
    public DBChanges ( Version version ) {
        this.version = version;
        this.init ( );
    }
    
    private void init ( ) {
        String dbver = Database.getDBVersion ( );
        System.out.println("DBVersion: "+dbver);
        
        String[] verParts = dbver.split("[.-]");
        int generation = 0;
        int year = 0;
        int month = 0;
        int build = 0;
        try {
            generation = Integer.parseInt(verParts[0]);
            year = Integer.parseInt(verParts[1].substring(0,2));
            month = Integer.parseInt(verParts[1].substring(2,4));
            build = Integer.parseInt(verParts[2]);
            
        } catch ( NumberFormatException ex ) {
            Proc.log ( DBChanges.class.getName ( ) , ex );
        }
        
        int target = this.convertVersion ( this.version.getGeneration(), this.version.getYear(), this.version.getMonth(), this.version.getBuild() );
        int current = this.convertVersion ( generation, year, month, build );

        if ( target > current ) {
            build++;
            while ( ! this.applyChanges ( generation, year, month, build++ ) ) {
                if ( build > 99 ) {
                    build = 1;
                    month++;
                }
                if ( month > 12 ) {
                    month = 1;
                    year++;
                }
                if ( year > 99 ) {
                    year = 0;
                    generation++;
                }
            }
        }
        System.exit ( 1 );
 
    }
        
    private int convertVersion ( int gen, int year, int month, int build ) {
        return ( gen*100000 + year*1000 + month*10 + build );
    }
    
    private boolean applyChanges ( int gen, int year, int month, int build ) {
        int target = this.convertVersion(gen, year, month, build);
        int counter = 0;
        ArrayList<String> qList = new ArrayList<>();
        switch ( target ) {
            case 117021 :
                qList.add ( "to: v1.1702-1 (Initial version)");
                qList.addAll ( this.db117021 ( ) );
                qList.add ( "update settings set value = '1.1702-1' where name = 'version'" );

            case 118011 :
                qList.add ( "to: v1.1801-1 (Base version)");
                qList.addAll ( this.db118011 ( ) );
                qList.add ( "update settings set value = '1.1801-1' where name = 'version'" );
            
            case 118091 :
                qList.add ( "to: v1.1809-1");
                qList.add ( "update settings set value = '1.1809-1' where name = 'version'" );
                
                break;
                
            default :
                return false;
                 
        }
        for ( String query : qList ) {
            if ( query.matches ( "^to: (.*)" ) ) {
                System.out.print ( "\n" );
                System.out.print ( "Updating DB "+query+" ..." );
                counter = 0;
            } else {
                if ( ! Database.change ( query ) ) { 
                    System.out.println ( ": "+query );
                    System.out.println ( "  - Change FAILED to apply" );
                    System.exit ( 1 );
                } else {
                    if ( counter++ % 10 == 0 ) {
                        System.out.print ( "." );
                    }
                }
            }
        }
        System.out.print ( "\n" );
        return true;
    }
     
    private ArrayList<String> db117021 ( ) {
        ArrayList<String> qList = new ArrayList<>();
        qList.add ( "CREATE TABLE akill (id int(11) NOT NULL AUTO_INCREMENT, mask varchar(64) DEFAULT NULL, reason varchar(256) DEFAULT NULL, instater varchar(33) DEFAULT NULL, stamp int(11) DEFAULT NULL, expire int(11) DEFAULT NULL, PRIMARY KEY (id)) ENGINE=InnoDB AUTO_INCREMENT=313 DEFAULT CHARSET=latin1;");
        qList.add ( "CREATE TABLE cflags (name varchar(33) DEFAULT NULL, type varchar(16) DEFAULT NULL, reason varchar(256) DEFAULT NULL, instater varchar(32) DEFAULT NULL, stamp int(11) DEFAULT NULL, expire int(11) DEFAULT NULL) ENGINE=InnoDB DEFAULT CHARSET=latin1;");
        qList.add ( "CREATE TABLE chan ( name varchar(33) NOT NULL DEFAULT '', founder varchar(32) DEFAULT NULL, pass varchar(32) DEFAULT NULL, description varchar(128) DEFAULT NULL, regstamp int(11) DEFAULT NULL, stamp int(11) DEFAULT NULL, PRIMARY KEY (name)) ENGINE=InnoDB DEFAULT CHARSET=latin1;" );
        qList.add ( "CREATE TABLE chanaccess ( name varchar(33) NOT NULL DEFAULT '', access varchar(12) DEFAULT NULL, nick varchar(160) NOT NULL DEFAULT '', instater varchar(32) DEFAULT NULL, stamp int(11) DEFAULT NULL, PRIMARY KEY (name,nick)) ENGINE=InnoDB DEFAULT CHARSET=latin1;" );
        qList.add ( "CREATE TABLE chansetting (name varchar(33) NOT NULL DEFAULT '', keeptopic tinyint(1) DEFAULT NULL, topiclock varchar(16) DEFAULT 'OFF', ident tinyint(1) DEFAULT NULL, opguard tinyint(1) DEFAULT NULL, restricted tinyint(1) DEFAULT NULL, verbose tinyint(1) DEFAULT NULL,  mailblock tinyint(1) DEFAULT NULL,  leaveops tinyint(1) DEFAULT NULL,  private tinyint(1) DEFAULT NULL,  modelock varchar(16) DEFAULT NULL,  PRIMARY KEY (name)) ENGINE=InnoDB DEFAULT CHARSET=latin1;" );
        qList.add ( "CREATE TABLE chantopic ( name varchar(33) NOT NULL DEFAULT '', setter varchar(32) DEFAULT NULL, stamp int(11) DEFAULT NULL, topic varchar(512) DEFAULT NULL, PRIMARY KEY (name)) ENGINE=InnoDB DEFAULT CHARSET=latin1;" ); 
        qList.add ( "CREATE TABLE command (id int(11) NOT NULL AUTO_INCREMENT, target varchar(33) DEFAULT NULL, targettype varchar(32) DEFAULT NULL, command varchar(32) DEFAULT NULL, PRIMARY KEY (id)) ENGINE=InnoDB DEFAULT CHARSET=latin1;" );
        qList.add ( "CREATE TABLE host (ip varchar(64) NOT NULL DEFAULT '', host varchar(128) DEFAULT NULL, stamp int(11) DEFAULT NULL, PRIMARY KEY (ip)) ENGINE=InnoDB DEFAULT CHARSET=latin1;" ); 
        qList.add ( "CREATE TABLE log (id int(11) NOT NULL AUTO_INCREMENT, target varchar(33) DEFAULT NULL, body text, stamp int(11) DEFAULT NULL, PRIMARY KEY (id)) ENGINE=InnoDB AUTO_INCREMENT=1261 DEFAULT CHARSET=latin1;" ); 
        qList.add ( "CREATE TABLE mailbox (id int(11) NOT NULL AUTO_INCREMENT, mail varchar(64) DEFAULT NULL, subject varchar(64) DEFAULT NULL, body text, stamp int(11) DEFAULT NULL, status int(11) DEFAULT NULL, PRIMARY KEY (id)) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=latin1;" );
        qList.add ( "CREATE TABLE memo (id int(11) NOT NULL AUTO_INCREMENT, name varchar(33) DEFAULT NULL, sender varchar(33) DEFAULT NULL, message varchar(256) DEFAULT NULL, readflag tinyint(1) DEFAULT '0', stamp int(11) DEFAULT NULL, PRIMARY KEY (id)) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=latin1;" );
        qList.add ( "CREATE TABLE nick (name varchar(32) NOT NULL DEFAULT '', hashcode int(11) DEFAULT NULL, mask varchar(128) DEFAULT NULL, pass varchar(32) DEFAULT NULL, mail varchar(64) DEFAULT NULL, auth varchar(32) DEFAULT NULL, regstamp int(11) DEFAULT NULL, stamp int(11) DEFAULT NULL, PRIMARY KEY (name)) ENGINE=InnoDB DEFAULT CHARSET=latin1;" );
        qList.add ( "CREATE TABLE nflags (name varchar(32) NOT NULL DEFAULT '', type varchar(16) NOT NULL DEFAULT '', reason varchar(256) DEFAULT NULL, instater varchar(32) DEFAULT NULL, stamp int(11) DEFAULT NULL, expire int(11) DEFAULT NULL,  PRIMARY KEY (name,type),  CONSTRAINT nflags_ibfk_1 FOREIGN KEY (name) REFERENCES nick (name) ON DELETE CASCADE ON UPDATE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=latin1;" );
        qList.add ( "CREATE TABLE nicksetting (name varchar(32) NOT NULL DEFAULT '', enforce tinyint(1) NOT NULL DEFAULT '0', secure tinyint(1) NOT NULL DEFAULT '0', private tinyint(1) NOT NULL DEFAULT '0', noop tinyint(1) NOT NULL DEFAULT '0', neverop tinyint(1) NOT NULL DEFAULT '0', mailblock tinyint(1) NOT NULL DEFAULT '0', showemail tinyint(1) NOT NULL DEFAULT '0', showhost tinyint(1) NOT NULL DEFAULT '0', auth tinyint(1) DEFAULT '0', PRIMARY KEY (name), CONSTRAINT nicksetting_ibfk_1 FOREIGN KEY (name) REFERENCES nick (name) ON DELETE CASCADE ON UPDATE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=latin1;" );
        qList.add ( "CREATE TABLE oper (name varchar(32) NOT NULL DEFAULT '', access int(11) DEFAULT NULL, instater varchar(32) DEFAULT NULL, PRIMARY KEY (name), CONSTRAINT oper_ibfk_1 FOREIGN KEY (name) REFERENCES nick (name) ON DELETE CASCADE ON UPDATE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=latin1;" );
        qList.add ( "CREATE TABLE settings (name varchar(32) NOT NULL,value varchar(64) DEFAULT NULL,PRIMARY KEY (name)) ENGINE=InnoDB DEFAULT CHARSET=latin1;" );
        qList.add ( "insert into settings (name,value) values ( 'version', '1.1701-1' );" );
        return qList;
    }
    private ArrayList<String> db118011 ( ) {
        ArrayList<String> qList = new ArrayList<>();
        String salt = Proc.getConf().get ( SECRETSALT );
        // Add constraints on channels
        qList.add ( "alter table chansetting add constraint foreign key (name) references chan (name) on delete cascade on update cascade;" );
        qList.add ( "alter table chanaccess add constraint foreign key (name) references chan (name) on delete cascade on update cascade;" );

        // Add nick log
        qList.add ( "create table nicklog (id int primary key auto_increment, name varchar(32), flag varchar(8), usermask varchar(160), oper varchar(32));" );

        // Add chan log
        qList.add ( "create table chanlog (id int primary key auto_increment, name varchar(32), flag varchar(8), usermask varchar(160), oper varchar(32));" );

        // Clean up log
        qList.add ( "delete from log where body like '%[SIDENTIFY]%';" );
        qList.add ( "delete from log where body like '%[IDEN%]%';" );
        qList.add ( "delete from log where body like '*[REGISTER]%';" );
        qList.add ( "delete from log where body like '%[CHANLIST]%';" );
        qList.add ( "delete from log where body like '%[AUTH]%';" );
        qList.add ( "delete from log where body like '%[OINFO]%';" );
        qList.add ( "delete from log where body like '%[UINFO]%';" );
        qList.add ( "delete from log where body like '%[LOGIN]%';" );
        qList.add ( "delete from log where body like '%[RESEND]%';" );
        qList.add ( "delete from log where body like '%[EMAIL]%';" );
        qList.add ( "delete from log where body like '%[VERIFY]%';" );
        qList.add ( "delete from log where body like '%[LIST]%';" );
        qList.add ( "delete from log where body like '%[SEND]%';" );


        // Convert stamp to datetime
        // alter table test add stamp2 datetime;
        // update test set stamp2=from_unixtime(stamp);
        // alter table test drop stamp;
        // alter table test change stamp2 stamp datetime;


        // Nick stamp
        qList.add ( "alter table nick add stamp2 datetime;" );
        qList.add ( "update nick set stamp2=from_unixtime(stamp);" );
        qList.add ( "alter table nick drop stamp;" );
        qList.add ( "alter table nick change stamp2 stamp datetime;" );

        // Nick regstamp
        qList.add ( "alter table nick add stamp2 datetime;" );
        qList.add ( "update nick set stamp2=from_unixtime(regstamp);" );
        qList.add ( "alter table nick drop regstamp;" );
        qList.add ( "alter table nick change stamp2 regstamp datetime;" );


        // Chan stamp
        qList.add ( "alter table chan add stamp2 datetime;" );
        qList.add ( "update chan set stamp2=from_unixtime(stamp);" );
        qList.add ( "alter table chan drop stamp;" );
        qList.add ( "alter table chan change stamp2 stamp datetime;" );

        // Chan regstamp
        qList.add ( "alter table chan add stamp2 datetime;" );
        qList.add ( "update chan set stamp2=from_unixtime(regstamp);" );
        qList.add ( "alter table chan drop regstamp;" );
        qList.add ( "alter table chan change stamp2 regstamp datetime;" );

        // Encrypt passwords
        qList.add ( "update chan set pass=aes_encrypt(pass,'"+salt+"');" );
        qList.add ( "update nick set pass=aes_encrypt(pass,'"+salt+"');" );

        // Decrypting passwords example
        // select name,aes_decrypt(pass,'') as pass from nick;" );

        // Encrypt emails
        qList.add ( "update nick set mail=aes_encrypt(mail,'"+salt+"');" );

        // Nick Expire
        qList.add ( "create table nickexp (name varchar(32),lastsent int,mailcount int, primary key (name), constraint foreign key (name) references nick (name) on delete cascade on update cascade) engine=InnoDB charset=latin1;" );

        // Chanacclog
        qList.add ( "create table chanacclog (id int primary key auto_increment, name varchar(32), nick varchar(32), oldaccess varchar(8) default null, access varchar(8), instater varchar(32), stamp datetime default now());" );
        qList.add ( "INSERT INTO chanacclog (name,nick,access,instater,stamp) select c.name,c.nick,c.access,c.instater,from_unixtime(c.stamp) from chanaccess as c;" );
         
        // Cleanup access lists
        qList.add ( "alter table chanaccess drop instater;" );
        qList.add ( "alter table chanaccess drop stamp;" );

        // Add channel registrations to the chanlog
        qList.add ( "alter table chanlog add stamp datetime after oper;" );
        qList.add ( "insert into chanlog (name,flag,usermask,stamp) select c.name,'R',concat(n.name,'!',n.mask),c.regstamp from chan as c join nick as n on n.name = c.founder;" );

        // Add nick registrations to the nicklog
        qList.add ( "alter table nicklog add stamp datetime after oper;" );
        qList.add ( "insert into nicklog (name,flag,usermask,stamp) select name,'R',concat(name,'!',mask),regstamp from nick;" );

        // Add oper log
        qList.add ( "create table operlog (id int primary key auto_increment, name varchar(32), flag varchar(8), usermask varchar(160), oper varchar(32));" );
        qList.add ( "alter table operlog add stamp datetime after oper;" );
 
        // Drop table cflags
        qList.add ( "drop table cflags;" );

        // Drop table nflags
        qList.add ( "drop table nflags;" );

        // Fixing chansetting
        qList.add ( "alter table chansetting add freeze tinyint(1) default 0;" );
        qList.add ( "alter table chansetting change keeptopic keeptopic tinyint(1) default 0;" );
        qList.add ( "alter table chansetting change ident ident tinyint(1) default 0;" );
        qList.add ( "alter table chansetting change opguard opguard tinyint(1) default 0;" );
        qList.add ( "alter table chansetting change restricted restricted tinyint(1) default 0;" );
        qList.add ( "alter table chansetting change verbose verbose tinyint(1) default 0;" );
        qList.add ( "alter table chansetting change mailblock mailblock tinyint(1) default 0;" );
        qList.add ( "alter table chansetting change leaveops leaveops tinyint(1) default 0;" );
        qList.add ( "alter table chansetting change private private tinyint(1) default 0;" );
        qList.add ( "alter table chansetting change modelock modelock tinyint(1) default 0;" );
        qList.add ( "alter table chansetting add close tinyint(1) default 0;" );
        qList.add ( "alter table chansetting add hold tinyint(1) default 0;" );

        // Fix nicksetting
        qList.add ( "alter table nicksetting drop enforce;" );
        qList.add ( "alter table nicksetting drop secure;" );
        qList.add ( "alter table nicksetting drop private;" );

        // Mark
        qList.add ( "alter table nicksetting add mark tinyint(1) default 0;" );

        // Comment
        qList.add ( "create table comment (id int primary key auto_increment, name varchar(33), instater varchar(32), comment varchar(512), stamp datetime);" );

        // Fix chanacclog
        qList.add ( "alter table chanacclog drop oldaccess;" );
        qList.add ( "alter table chanacclog add usermask varchar(110) after instater;" );

        // Fix data lengths based on bahamut lengths
        qList.add ( "alter table akill change mask mask varchar(110);" );
        qList.add ( "alter table chanaccess change nick nick varchar(110);" );
        qList.add ( "alter table chanlog change usermask usermask varchar(110);" );
        qList.add ( "alter table chantopic change topic topic varchar(308);" );
        qList.add ( "alter table chantopic change setter setter varchar(110);" );
        qList.add ( "alter table host change host host varchar(64);" );
        qList.add ( "alter table host change ip ip varchar(46);" );
        qList.add ( "alter table mailbox change mail mail varchar(256);" );
        qList.add ( "alter table nick change mail mail varchar(256);" );
        qList.add ( "alter table nick change mask mask varchar(80);" );
        qList.add ( "alter table nicklog change usermask usermask varchar(110);" );
        qList.add ( "alter table operlog change usermask usermask varchar(110);" );


        // Fix akill stamps
        qList.add ( "alter table akill change stamp stamp datetime;" );
        qList.add ( "alter table akill change expire expire datetime;" );


        // Fix chanacclog
        qList.add ( "alter table chanacclog change nick target varchar(32);" );
        qList.add ( "alter table chanacclog drop usermask;" );
        qList.add ( "alter table chanacclog add usermask varchar(110) after instater;" );

        // Add globallog
        qList.add ( "create table globallog (id int primary key, global varchar(512), constraint foreign key (id) references operlog (id) on delete cascade on update cascade);" );

        // add ignorelist
        qList.add ( "create table ignorelist (id int primary key auto_increment,mask varchar(110),reason varchar(256),instater varchar(33),stamp datetime,expire datetime);" );

        // fix operlog
        qList.add ( "alter table operlog add data varchar(512);" );

        // create banlog
        qList.add ( "CREATE TABLE banlog ( id int primary key auto_increment, ticket varchar(32), flag varchar(8), usermask varchar(110), oper varchar(32), stamp datetime, data varchar(512) );" );

        // add sqline
        qList.add ( "create table sqline (id int primary key auto_increment,mask varchar(110),reason varchar(256),instater varchar(33),stamp datetime,expire datetime);" );

        // add sgline
        qList.add ( "create table sgline (id int primary key auto_increment,mask varchar(110),reason varchar(256),instater varchar(33),stamp datetime,expire datetime);" );
 
        // Add server
        qList.add ( "create table server (name varchar(64) primary key,lastseen datetime);" );

        // Add auditorium mode
        qList.add ( "alter table chansetting add auditorium tinyint default 0;" );

        // Fix modelock
        qList.add ( "alter table chansetting change modelock modelock varchar(16);" );

        // Fix chanacclog
        qList.add ( "alter table chanacclog change target target varchar(110) default null;" );

        // Fix nicksetting operflags
        qList.add ( "alter table nicksetting add freeze varchar(32) default null;" );
        qList.add ( "alter table nicksetting add hold varchar(32) default null;" );
        qList.add ( "alter table nicksetting change mark mark varchar(32) default null;" );
        qList.add ( "update nicksetting set freeze=NULL, hold=NULL,mark=NULL;" );

        // Fix chansetting operflags
        qList.add ( "alter table chansetting change freeze freeze varchar(32) default null;" );
        qList.add ( "alter table chansetting change hold hold varchar(32) default null;" );
        qList.add ( "alter table chansetting add mark varchar(32) default null after hold;" );
        qList.add ( "alter table chansetting change close close varchar(32) default null;" );
        qList.add ( "alter table chansetting change auditorium auditorium varchar(32) default null;" );
        qList.add ( "update chansetting set freeze=NULL, hold=NULL,mark=NULL,close=NULL,auditorium=NULL;" );

        // Add maillog
        qList.add ( "create table maillog ( id int primary key auto_increment, nick varchar(32), mail varchar(256), auth varchar(33), stamp datetime ) ENGINE=InnoDB DEFAULT CHARSET=latin1;" );
        qList.add ( "insert into maillog (nick,mail,auth,stamp) select name,mail,NULL,regstamp from nick;" );
        qList.add ( "alter table nick drop mail;" );
        qList.add ( "alter table nick drop auth;" );

        // Add passlog
        qList.add ( "create table passlog ( id int primary key auto_increment, nick varchar(32), pass varchar(64), auth varchar(33), stamp datetime ) ENGINE=InnoDB DEFAULT CHARSET=latin1;" );
        qList.add ( "insert into passlog (nick,pass,auth,stamp) select name,pass,NULL,regstamp from nick;" );
        qList.add ( "alter table nick drop pass;" );

        // Add extra to command
        qList.add ( "alter table command add extra varchar(64);" );

        // Add noghost
        qList.add ( "alter table nicksetting add noghost varchar(32);" );

        // Remove auth on nicks
        qList.add ( "alter table nicksetting drop auth;" );

        // Add topiclog and move topics from chan
        qList.add ( "create table topiclog (name varchar(33), setter varchar(110), stamp datetime, topic varchar(308));" );
        qList.add ( "insert into topiclog (select name,setter,from_unixtime(stamp),topic from chantopic);" );
        qList.add ( "drop table chantopic;" );
 
        // Add persistent servicesID's
        qList.add ( "create table servicesid ( id bigint primary key, stamp datetime, nicks varchar(128), chans varchar(128) );" );


        // Add primary and seconday hub to server list
        qList.add ( "alter table server add primaryhub varchar(64) after name;" );
        qList.add ( "alter table server add secondaryhub varchar(64) after primaryhub; " );
        qList.add ( "alter table server drop lastseen;" );


        // Add chanflags table for eXtended flags
        qList.add ( "create table chanflag (name varchar(32), join_connect_time smallint, talk_connect_time smallint, talk_join_time smallint, max_bans smallint, no_notice tinyint(1), no_ctcp tinyint(1), no_part_msg tinyint(1), no_quit_msg tinyint(1), exempt_opped tinyint(1), exempt_voiced tinyint(1), exempt_identd tinyint(1), exempt_registered tinyint(1), exempt_invites tinyint(1), greetmsg varchar(256), primary key (name) );" );

        // Add chanflag for all current chans
        qList.add ( "insert into chanflag (select name,0,0,0,200,0,0,0,0,0,0,0,0,0,null from chan);" );

        // Chansettingfix
        qList.add ( "alter table chansetting drop private;" );
        
        return qList;
    }
}
