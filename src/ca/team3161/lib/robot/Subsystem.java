/* Copyright (c) 2014, FRC3161
* All rights reserved.
* 
* Redistribution and use in source and binary forms, with or without modification,
* are permitted provided that the following conditions are met:
* 
* * Redistributions of source code must retain the above copyright notice, this
*   list of conditions and the following disclaimer.
* 
* * Redistributions in binary form must reproduce the above copyright notice, this
*   list of conditions and the following disclaimer in the documentation and/or
*   other materials provided with the distribution.
* 
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
* ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
* ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
* ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package ca.team3161.lib.robot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.Lock;

/**
 * Abstracts a system which uses resourceLocks and has some task (recurring or
 * one-shot) to be performed. An example is PID control - monitor sensors
 * and periodically set motor values based on this.
 */
public abstract class Subsystem {

    protected static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(4);

    /**
     * A list of resourceLocks which this Subsystem requires.
     * @see ca.team3161.lib.robot.ResourceTracker
     */
    protected final List<Lock> resourceLocks = new ArrayList<>();
    
    /**
     * Define a required resource for this Subsystem when its task is executed.
     * @param resource a sensor, speed controller, etc. that this subsystem
     * needs exclusive access to during its task
     */
    protected final void require(final Object resource) {
        Objects.requireNonNull(resource);
        resourceLocks.add(ResourceTracker.track(resource));
    }
    
    private void acquireResources() throws InterruptedException {
        resourceLocks.forEach(Lock::tryLock);
    }
    
    private void releaseResources() {
        resourceLocks.forEach(Lock::unlock);
    }
    
    /**
     * Check if this Subsystem's task has been canceled.
     * @return if this Subsystem's background task has been canceled
     */
    public final boolean getCancelled() {
        return (getJob() != null && getJob().isCancelled());
    }

    public final boolean getStarted() {
        return getJob() != null && (!getJob().isCancelled() && !getJob().isDone());
    }
    
    /**
     * Cancel the background task of this Subsystem (stop it from running, if it
     * is a recurring task).
     */
    public final void cancel() {
        if (getJob() != null) {
            getJob().cancel(true);
        }
    }
    
    /**
     * Start (or restart) this Subsystem's background task.
     */
    public abstract void start();

    /**
     * Get this subsystem's task.
     * @return the current task for this subsystem, if any
     */
    protected abstract Future<?> getJob();

    /**
     * The task for this Subsystem to run.
     */
    protected class RunTask implements Runnable {
        @Override
        public void run() {
            try {
                acquireResources();
                task();
            } catch (final Exception e) {
                e.printStackTrace();
            } finally {
                releaseResources();
            }
        }
    }
    
    /**
     * Define the set of resourceLocks required for this Subsystem's task.
     * @see ca.team3161.lib.robot.Subsystem#require(Object)
     */
    protected abstract void defineResources();
    
    /**
     * The background task to run.
     * @throws Exception in case the defined task throws any Exceptions
     */
    protected abstract void task() throws Exception;
    
}
