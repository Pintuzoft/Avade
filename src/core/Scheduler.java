/*
 * Copyright (C) 2018 Fredrik Karlsson aka DreamHealer & avade.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package core;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Delayed tasks (guest nick changes, reminders, unsqlines..).
 *
 * One timer thread only keeps time, the tasks themselves are run by the main
 * loop (Proc) so they never touch users, channels or the socket at the same
 * time as the main thread does.
 *
 * @author DreamHealer
 */
public class Scheduler {
    private static final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor ( r -> {
        Thread t = new Thread ( r, "avade-scheduler" );
        t.setDaemon ( true );
        return t;
    } );
    private static final ConcurrentLinkedQueue<Runnable> due = new ConcurrentLinkedQueue<>();

    /**
     * Run a task on the main thread after a delay
     * @param task
     * @param delayMs
     * @return handle that can be used to cancel the task
     */
    public static ScheduledFuture<?> schedule ( Runnable task, long delayMs ) {
        return timer.schedule ( ( ) -> due.add ( task ), delayMs, TimeUnit.MILLISECONDS );
    }

    /**
     * Cancel a scheduled task, null safe
     * @param handle
     */
    public static void cancel ( ScheduledFuture<?> handle ) {
        if ( handle != null ) {
            handle.cancel ( false );
        }
    }

    /**
     * Called from the main loop, runs the tasks that are due
     * @return number of tasks run
     */
    public static int runDue ( ) {
        int count = 0;
        Runnable task;
        while ( ( task = due.poll ( ) ) != null ) {
            try {
                task.run ( );
            } catch ( Exception e ) {
                Proc.log ( Scheduler.class.getName ( ), e );
            }
            count++;
        }
        return count;
    }
}
