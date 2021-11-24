package com.pi4j.mvc.util.mvcbase;

import java.time.Duration;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Base class for all Controllers.
 *
 * The whole application logic is located in controller classes.
 *
 * Controller classes work on and manage the Model. Models encapsulate the whole application state.
 *
 * Controllers provide the whole core functionality of the application, so called 'Actions'
 *
 * Execution of Actions is asynchronous. The sequence is kept stable, such that
 * for all actions A and B: if B is submitted after A, B will only be executed after A is finished.
 */
public abstract class ControllerBase<M> {

    private ConcurrentTaskQueue<M> actionQueue;

    // the model managed by this Controller. Only subclasses have direct access
    protected final M model;

    /**
     * Controller needs a Model.
     *
     * @param model Model managed by this Controller
     */
    protected ControllerBase(M model){
        Objects.requireNonNull(model);

        this.model = model;
    }

    public void shutdown(){
        if(null != actionQueue){
            actionQueue.shutdown();
            actionQueue = null;
        }
    }

    /**
     * Schedule the given action for execution in strict order in external thread, asynchronously.
     *
     * onDone is called as soon as action is finished
     */
    protected void async(Supplier<M> action, Consumer<M> onDone) {
        if(null == actionQueue){
            actionQueue = new ConcurrentTaskQueue<>();
        }
        actionQueue.submit(action, onDone);
    }


    /**
     * Schedule the given action for execution in strict order in external thread, asynchronously.
     *
     */
    protected void async(Runnable todo){
        async(() -> {
                todo.run();
                return model;
            },
            m -> {});
    }

    /**
     * Schedule the given action after all the actions already scheduled have finished.
     *
     */
    public void runLater(Consumer<M> action) {
        async(() -> model, action);
    }

    /**
     * Intermediate solution for TestCase support.
     *
     * Best solution would be that 'action' of 'runLater' is executed on calling thread.
     *
     * Waits until all current actions in actionQueue are completed.
     *
     * In most cases it's wrong to call this method from within an application.
     */
    public void awaitCompletion(){
        if(actionQueue == null){
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        actionQueue.submit( () -> {
            latch.countDown();
            return null;
        });
        try {
            //noinspection ResultOfMethodCallIgnored
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new IllegalStateException("CountDownLatch was interrupted");
        }
    }

    /**
     * Only the other base classes 'ViewMixin' and 'PUI_Base' need access, therefore it's 'package private'
     */
    M getModel() {
        return model;
    }

    /**
     * Even for setting a value the controller is responsible.
     *
     * No application specific class can access ObservableValue.setValue
     *
     * Value is set asynchronously.
     */
    protected <V> void setValue(ObservableValue<V> observableValue, V newValue){
        async(() -> observableValue.setValue(newValue));
    }

    /**
     * Convenience method to toggle a ObservableValue<Boolean>
     */
    protected void toggle(ObservableValue<Boolean> observableValue){
        async(() -> observableValue.setValue(!observableValue.getValue()));
    }

    /**
     * Convenience method to increase a ObservableValue<Integer> by 1
     */
    protected void increase(ObservableValue<Integer> observableValue){
        async(() -> observableValue.setValue(observableValue.getValue() + 1));
    }

    /**
     * Convenience method to decrease a ObservableValue<Integer> by 1
     */
    protected void decrease(ObservableValue<Integer> observableValue){
        async(() -> observableValue.setValue(observableValue.getValue() - 1));
    }

    /**
     * Utility function to pause execution of actions for the specified amount of time.
     *
     * An {@link InterruptedException} will be catched and ignored while setting the interrupt flag again.
     *
     * @param duration time to sleep
     */
    protected void pauseExecution(Duration duration) {
        async(() -> {
            try {
                Thread.sleep(duration.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
}
