package com.jzy.ai.btree;


import com.jzy.ai.btree.annotation.TaskConstraint;
import com.jzy.ai.util.IMemoryObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * 行为树抽象节点任务
 *
 * @param <E> 黑板对象，所属的对象，如NPC
 * @author davebaol
 * @QQ 359135103 2017年11月22日 下午2:33:32
 * @fix JiangZhiYong
 */
@TaskConstraint
public abstract class Task<E> implements IMemoryObject, Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(Task.class);

    /**
     * 任务状态
     */
    protected Status status = Status.FRESH;

    /**
     * 父节点
     */
    protected Task<E> control;

    /**
     * 该节点任务所属的行为树
     */
    protected BehaviorTree<E> tree;

    /**
     * 当前任务的防护条件
     */
    protected Task<E> guard;

    /**
     * 节点名称，调试识别
     */
    protected String name;

    /**
     * 添加子任务
     *
     * @param childTask
     * @return 子任务所在下标
     * @author JiangZhiYong
     * @QQ 359135103 2017年11月22日 下午2:54:08
     */
    public final int addChild(Task<E> childTask) {
        int index = addChildToTask(childTask);
        if (tree != null && tree.listeners != null) {
            tree.notifyChildAdded(childTask, index);
        }
        return index;
    }

    /**
     * 子任务个数
     *
     * @return
     * @author JiangZhiYong
     * @QQ 359135103 2017年11月22日 下午4:01:24
     */
    public abstract int getChildCount();

    /**
     * 向该任务添加子任务
     *
     * @param childTask 子任务
     * @return 任务下标
     * @author JiangZhiYong
     * @QQ 359135103 2017年11月22日 下午3:32:26
     */
    protected abstract int addChildToTask(Task<E> childTask);

    public E getObject() {
        if (tree == null) {
            throw new IllegalStateException("行为树对象未设置");
        }
        return tree.getObject();
    }

    public Task<E> getGuard() {
        return guard;
    }

    public void setGuard(Task<E> guard) {
        this.guard = guard;
    }

    public final Status getStatus() {
        return status;
    }

    /**
     * 设置父任务
     *
     * @author JiangZhiYong
     * @QQ 359135103 2017年11月22日 下午4:06:58
     */
    public void setControl(Task<E> control) {
        this.control = control;
        this.tree = control.tree;
    }

    /**
     * 检查条件
     *
     * @param parentTask 父任务
     * @return
     * @author JiangZhiYong
     * @QQ 359135103 2017年11月22日 下午4:09:57
     */
    public boolean checkGuard(Task<E> parentTask) {
        // No guard to check
        if (guard == null) {
            return true;
        }

        // Check the guard of the guard recursively
        if (!guard.checkGuard(parentTask)) {
            return false;
        }

        // Use the tree's guard evaluator task to check the guard of this task
        guard.setControl(parentTask.tree.guardEvaluator);
        guard.start();
        guard.run();
        switch (guard.getStatus()) {
            case SUCCEEDED:
                return true;
            case FAILED:
                return false;
            default:
                throw new IllegalStateException("Illegal guard status '" + guard.getStatus()
                        + "'. Guards must either succeed or fail in one step.");
        }
    }

    /**
     * This method will be called once before this task's first run.
     */
    public void start() {
    }

    /**
     * This method will be called by {@link #success()}, {@link #fail()} or
     * {@link #cancel()}, meaning that this task's status has just been set to
     * {@link Status#SUCCEEDED}, {@link Status#FAILED} or {@link Status#CANCELLED}
     * respectively.
     */
    public void end() {
    }

    /**
     * This method contains the update logic of this task. The actual implementation
     * MUST call {@link #running()}, {@link #success()} or {@link #fail()} exactly
     * once.
     */
    public abstract void run();

    /**
     * This method will be called in {@link #run()} to inform control that this task
     * needs to run again
     */
    public final void running() {
        updateStatus(Status.RUNNING);

        if (control != null) {
            // 调用父节点的子节点运行
            control.childRunning(this, this);
        }

    }

    /**
     * This method will be called when one of the ancestors of this task needs to
     * run again
     *
     * @param runningTask the task that needs to run again
     * @param reporter    the task that reports, usually one of this task's children
     */
    public abstract void childRunning(Task<E> runningTask, Task<E> reporter);

    /**
     * This method will be called in {@link #run()} to inform control that this task
     * has finished running with a success result
     */
    public final void success() {
        updateStatus(Status.SUCCEEDED);

        end();
        if (control != null) {
            control.childSuccess(this);
        }
    }

    /**
     * This method will be called in {@link #run()} to inform control that this task
     * has finished running with a failure result
     */
    public final void fail() {
        updateStatus(Status.FAILED);

        end();
        if (control != null) {
            control.childFail(this);
        }
    }

    /**
     * This method will be called when one of the children of this task succeeds
     *
     * @param task the task that succeeded
     */
    public abstract void childSuccess(Task<E> task);

    /**
     * This method will be called when one of the children of this task fails
     *
     * @param task the task that failed
     */
    public abstract void childFail(Task<E> task);

    /**
     * Terminates this task and all its running children. This method MUST be called
     * only if this task is running.
     */
    public final void cancel() {
        cancelRunningChildren(0);
        Status previousStatus = status;
        status = Status.CANCELLED;
        if (tree.listeners != null && tree.listeners.size() > 0)
            tree.notifyStatusUpdated(this, previousStatus);
        end();
    }

    /**
     * Terminates the running children of this task starting from the specified
     * index up to the end.
     *
     * @param startIndex the start index
     */
    protected void cancelRunningChildren(int startIndex) {
        for (int i = startIndex, n = getChildCount(); i < n; i++) {
            Task<E> child = getChild(i);
            if (child.status == Status.RUNNING) {
                child.cancel();
            }
        }
    }

    /**
     * Returns the child at the given index.
     */
    public abstract Task<E> getChild(int i);

    /**
     * Resets this task to make it restart from scratch on next run.
     */
    public void resetTask() {
        if (status == Status.RUNNING) {
            cancel();
        }
        for (int i = 0, n = getChildCount(); i < n; i++) {
            getChild(i).resetTask();
        }
        status = Status.FRESH;
        tree = null;
        control = null;
    }

    @Override
    public void release() {
        control = null;
        guard = null;
        status = Status.FRESH;
        tree = null;

    }

    public String getName() {
        return name;
    }

    /**
     * 节点名称，调试识别
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 更新状态
     *
     * @param newStatus 新状态
     */
    private void updateStatus(Status newStatus) {
        // 上一个状态
        Status previousStatus = status;
        // 当前状态
        status = newStatus;
        if (tree.listeners != null && tree.listeners.size() > 0) {
            // 通知状态更新
            tree.notifyStatusUpdated(this, previousStatus);
        }
    }

    /**
     * 节点状态 <br>
     * The enumeration of the values that a task's status can have.
     *
     * @author davebaol
     */
    public enum Status {
        /**
         * 没有运行过,也没有被重置过
         */
        FRESH,
        /**
         * Means that the task needs to run again.
         */
        RUNNING,
        /**
         * Means that the task returned a failure result.
         */
        FAILED,
        /**
         * Means that the task returned a success result.
         */
        SUCCEEDED,
        /**
         * Means that the task has been terminated by an ancestor.
         */
        CANCELLED;
    }
}
