/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package priv.bigant.intrance.common;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Base implementation of the {@link Lifecycle} interface that implements the state transition rules for {@link
 * Lifecycle#start()} and {@link Lifecycle#stop()}
 */
public abstract class LifecycleBase implements Lifecycle {

    private static final Logger log = LoggerFactory.getLogger(LifecycleBase.class);

    /**
     * The list of registered LifecycleListeners for event notifications.
     */
    private final List<LifecycleListener> lifecycleListeners = new CopyOnWriteArrayList<>();


    /**
     * The current state of the source component.
     */
    private volatile LifecycleState state = LifecycleState.NEW;


    /**
     * {@inheritDoc}
     */
    @Override
    public void addLifecycleListener(LifecycleListener listener) {
        lifecycleListeners.add(listener);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public LifecycleListener[] findLifecycleListeners() {
        return lifecycleListeners.toArray(new LifecycleListener[0]);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void removeLifecycleListener(LifecycleListener listener) {
        lifecycleListeners.remove(listener);
    }


    /**
     * Allow sub classes to fire {@link Lifecycle} events.
     * <p>
     * 通知状态监听器状态流转
     *
     * @param type Event type
     * @param data Data associated with event.
     */
    protected void fireLifecycleEvent(String type, Object data) {
        LifecycleEvent event = new LifecycleEvent(this, type, data);
        for (LifecycleListener listener : lifecycleListeners) {
            listener.lifecycleEvent(event);
        }
    }


    @Override
    public final synchronized void init() throws LifecycleException {
        if (!state.equals(LifecycleState.NEW)) { //防止重复初始化
            invalidTransition(Lifecycle.BEFORE_INIT_EVENT);
        }

        try {
            //更改状态初始化中
            setStateInternal(LifecycleState.INITIALIZING, null, false);
            //执行初始化方法
            initInternal();
            //更改状态初始化结束
            setStateInternal(LifecycleState.INITIALIZED, null, false);
        } catch (Throwable t) {
            setStateInternal(LifecycleState.FAILED, null, false);
            throw new LifecycleException("lifecycleBase.initFail", t);
        }
    }


    protected abstract void initInternal() throws LifecycleException;

    /**
     * {@inheritDoc}
     * <p>
     * 重点：线程安全
     */
    @Override
    public final synchronized void start() throws LifecycleException {

        //正在运行或者运行已运行   不再重复执行
        if (LifecycleState.STARTING_PREP.equals(state) || LifecycleState.STARTING.equals(state) || LifecycleState.STARTED.equals(state)) {
            if (log.isDebugEnabled()) {
                Exception e = new LifecycleException();
                log.debug("lifecycleBase.alreadyStarted", e);
            } else if (log.isInfoEnabled()) {
                log.info("lifecycleBase.alreadyStarted");
            }
            return;
        }

        if (state.equals(LifecycleState.NEW)) {//根据状态判断应初始化
            init();
        } else if (state.equals(LifecycleState.FAILED)) { //失败应关闭
            stop();
        } else if (!state.equals(LifecycleState.INITIALIZED) && !state.equals(LifecycleState.STOPPED)) {
            //TODO
            invalidTransition(Lifecycle.BEFORE_START_EVENT);
        }

        try {
            //更改运行中状态
            setStateInternal(LifecycleState.STARTING_PREP, null, false);
            //运行方法
            startInternal();
            if (state.equals(LifecycleState.FAILED)) {//如果失败则关闭清理
                // This is a 'controlled' failure. The component put itself into the
                // FAILED state so call stop() to complete the clean-up.
                stop();
            } else if (!state.equals(LifecycleState.STARTING)) { //启动状态为流转到下一状态关闭
                // Shouldn't be necessary but acts as a check that sub-classes are
                // doing what they are supposed to.
                invalidTransition(Lifecycle.AFTER_START_EVENT);
            } else {//执行完成
                setStateInternal(LifecycleState.STARTED, null, false);
            }
        } catch (Throwable t) {
            // This is an 'uncontrolled' failure so put the component into the
            // FAILED state and throw an exception.
            setStateInternal(LifecycleState.FAILED, null, false);
            throw new LifecycleException("lifecycleBase.startFail", t);
        }
    }


    /**
     * Sub-classes must ensure that the state is changed to {@link LifecycleState#STARTING} during the execution of this
     * method. Changing state will trigger the {@link Lifecycle#START_EVENT} event.
     * <p>
     * If a component fails to start it may either throw a {@link LifecycleException} which will cause it's parent to
     * fail to start or it can place itself in the error state in which case {@link #stop()} will be called on the
     * failed component but the parent component will continue to start normally.
     *
     * @throws LifecycleException Start error occurred
     */
    protected abstract void startInternal() throws LifecycleException;


    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized void stop() throws LifecycleException {

        //正在关闭或者已关闭已运行   不再重复执行
        if (LifecycleState.STOPPING_PREP.equals(state) || LifecycleState.STOPPING.equals(state) || LifecycleState.STOPPED.equals(state)) {
            if (log.isDebugEnabled()) {
                Exception e = new LifecycleException();
                log.debug("lifecycleBase.alreadyStopped", e);
            } else if (log.isInfoEnabled()) {
                log.info("lifecycleBase.alreadyStopped");
            }

            return;
        }

        if (state.equals(LifecycleState.NEW)) {
            state = LifecycleState.STOPPED;
            return;
        }

        if (!state.equals(LifecycleState.STARTED) && !state.equals(LifecycleState.FAILED)) { //只有成功运行 和失败状态可关闭
            invalidTransition(Lifecycle.BEFORE_STOP_EVENT);
        }

        try {
            if (state.equals(LifecycleState.FAILED)) {
                // Don't transition to STOPPING_PREP as that would briefly mark the
                // component as available but do ensure the BEFORE_STOP_EVENT is
                // fired
                fireLifecycleEvent(BEFORE_STOP_EVENT, null);
            } else {
                setStateInternal(LifecycleState.STOPPING_PREP, null, false);
            }

            stopInternal();

            // Shouldn't be necessary but acts as a check that sub-classes are
            // doing what they are supposed to.
            if (!state.equals(LifecycleState.STOPPING) && !state.equals(LifecycleState.FAILED)) {
                invalidTransition(Lifecycle.AFTER_STOP_EVENT);
            }

            setStateInternal(LifecycleState.STOPPED, null, false);
        } catch (Throwable t) {
            setStateInternal(LifecycleState.FAILED, null, false);
            throw new LifecycleException("lifecycleBase.stopFail", t);
        } finally {
            if (this instanceof Lifecycle.SingleUse) {
                // Complete stop process first
                setStateInternal(LifecycleState.STOPPED, null, false);
                destroy();
            }
        }
    }


    /**
     * Sub-classes must ensure that the state is changed to {@link LifecycleState#STOPPING} during the execution of this
     * method. Changing state will trigger the {@link Lifecycle#STOP_EVENT} event.
     *
     * @throws LifecycleException Stop error occurred
     */
    protected abstract void stopInternal() throws LifecycleException;


    @Override
    public final synchronized void destroy() throws LifecycleException {
        if (LifecycleState.FAILED.equals(state)) {
            try {
                // Triggers clean-up
                stop();
            } catch (LifecycleException e) {
                // Just log. Still want to destroy.
                log.error("lifecycleBase.stopFail", e);
            }
        }

        if (LifecycleState.DESTROYING.equals(state) || LifecycleState.DESTROYED.equals(state)) {
            if (log.isDebugEnabled()) {
                Exception e = new LifecycleException();
                log.debug("lifecycleBase.stopFail", e);
            } else if (log.isInfoEnabled() && !(this instanceof Lifecycle.SingleUse)) {
                // Rather than have every component that might need to call
                // destroy() check for SingleUse, don't log an info message if
                // multiple calls are made to destroy()
                log.info("lifecycleBase.stopFail");
            }
            return;
        }

        if (!state.equals(LifecycleState.STOPPED) && !state.equals(LifecycleState.FAILED) && !state.equals(LifecycleState.NEW) && !state.equals(LifecycleState.INITIALIZED)) {
            invalidTransition(Lifecycle.BEFORE_DESTROY_EVENT);
        }

        try {
            setStateInternal(LifecycleState.DESTROYING, null, false);
            destroyInternal();
            setStateInternal(LifecycleState.DESTROYED, null, false);
        } catch (Throwable t) {
            setStateInternal(LifecycleState.FAILED, null, false);
            throw new LifecycleException("lifecycleBase.destroyFail", t);
        }
    }


    protected abstract void destroyInternal() throws LifecycleException;

    /**
     * {@inheritDoc}
     */
    @Override
    public LifecycleState getState() {
        return state;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getStateName() {
        return getState().toString();
    }


    /**
     * Provides a mechanism for sub-classes to update the component state. Calling this method will automatically fire
     * any associated {@link Lifecycle} event. It will also check that any attempted state transition is valid for a
     * sub-class.
     *
     * @param state The new state for this component
     * @throws LifecycleException when attempting to set an invalid state
     */
    protected synchronized void setState(LifecycleState state) throws LifecycleException {
        setStateInternal(state, null, true);
    }


    /**
     * Provides a mechanism for sub-classes to update the component state. Calling this method will automatically fire
     * any associated {@link Lifecycle} event. It will also check that any attempted state transition is valid for a
     * sub-class.
     *
     * @param state The new state for this component
     * @param data  The data to pass to the associated {@link Lifecycle} event
     * @throws LifecycleException when attempting to set an invalid state
     */
    protected synchronized void setState(LifecycleState state, Object data) throws LifecycleException {
        setStateInternal(state, data, true);
    }

    /**
     * 流转状态时执行的方法
     *
     * @param state
     * @param data
     * @param check
     * @throws LifecycleException
     */
    private synchronized void setStateInternal(LifecycleState state, Object data, boolean check) throws LifecycleException {

        if (log.isDebugEnabled()) {
            log.debug("lifecycleBase.setState");
        }

        if (check) {
            // Must have been triggered by one of the abstract methods (assume
            // code in this class is correct)
            // null is never a valid state
            if (state == null) {
                invalidTransition("null");
                // Unreachable code - here to stop eclipse complaining about
                // a possible NPE further down the method
                return;
            }

            // Any method can transition to failed
            // startInternal() permits STARTING_PREP to STARTING
            // stopInternal() permits STOPPING_PREP to STOPPING and FAILED to
            // STOPPING
            if (!(state == LifecycleState.FAILED ||
                    (this.state == LifecycleState.STARTING_PREP && state == LifecycleState.STARTING) ||
                    (this.state == LifecycleState.STOPPING_PREP && state == LifecycleState.STOPPING) ||
                    (this.state == LifecycleState.FAILED && state == LifecycleState.STOPPING))) {//检验流转状态是否正常
                // No other transition permitted
                invalidTransition(state.name());
            }
        }

        this.state = state;
        String lifecycleEvent = state.getLifecycleEvent();
        if (lifecycleEvent != null) {
            fireLifecycleEvent(lifecycleEvent, data);
        }
    }


    /**
     * 如果状态流转不对将执行此方法抛出异常
     *
     * @param type
     * @throws LifecycleException
     */
    private void invalidTransition(String type) throws LifecycleException {
        throw new LifecycleException("lifecycleBase.invalidTransition" + type + toString() + state);
    }
}
