package com.jzy.ai.btree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 行为树 <br>
 *
 * @author implicit-invocation
 * @author davebaol
 * @fix JiangZhiYong
 * @QQ 359135103 2017年11月22日 下午2:43:23
 */
public class BehaviorTree<E> extends Task<E> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BehaviorTree.class);
    /**
     * 监听器
     */
    public List<Listener<E>> listeners;
    /**
     * 安全监测
     */
    GuardEvaluator<E> guardEvaluator;
    /**
     * 根任务
     */
    private Task<E> rootTask;
    /**
     * 行为树所属对象
     */
    private E object;

    /**
     * Creates a {@code BehaviorTree} with no root task and no blackboard object. Both the root task and the blackboard
     * object must be set before running this behavior tree, see {@link #addChild(Task) addChild()} and
     * {@link #setObject(Object) setObject()} respectively.
     */
    public BehaviorTree() {
        this(null, null);
    }

    /**
     * Creates a behavior tree with a root task and no blackboard object. Both the root task and the blackboard object
     * must be set before running this behavior tree, see {@link #addChild(Task) addChild()} and
     * {@link #setObject(Object) setObject()} respectively.
     *
     * @param rootTask the root task of this tree. It can be {@code null}.
     */
    public BehaviorTree(Task<E> rootTask) {
        this(rootTask, null);
    }

    /**
     * Creates a behavior tree with a root task and a blackboard object. Both the root task and the blackboard object
     * must be set before running this behavior tree, see {@link #addChild(Task) addChild()} and
     * {@link #setObject(Object) setObject()} respectively.
     *
     * @param rootTask the root task of this tree. It can be {@code null}.
     * @param object   the blackboard. It can be {@code null}.
     */
    public BehaviorTree(Task<E> rootTask, E object) {
        this.rootTask = rootTask;
        this.object = object;
        this.tree = this;
        this.guardEvaluator = new GuardEvaluator<E>(this);
    }

    /**
     * Returns the blackboard object of this behavior tree.
     */
    @Override
    public E getObject() {
        return object;
    }

    /**
     * Sets the blackboard object of this behavior tree.
     *
     * @param object the new blackboard
     */
    public void setObject(E object) {
        this.object = object;
    }

    /**
     * This method will add a child, namely the root, to this behavior tree.
     *
     * @param child the root task to add
     * @return the index where the root task has been added (always 0).
     * @throws IllegalStateException if the root task is already set.
     */
    @Override
    protected int addChildToTask(Task<E> child) {
        if (this.rootTask != null)
            throw new IllegalStateException("A behavior tree cannot have more than one root task");
        this.rootTask = child;
        return 0;
    }

    @Override
    public int getChildCount() {
        return rootTask == null ? 0 : 1;
    }

    @Override
    public Task<E> getChild(int i) {
        if (i == 0 && rootTask != null) {
            return rootTask;
        }
        throw new IndexOutOfBoundsException("index can't be >= size: " + i + " >= " + getChildCount());
    }

    @Override
    public void childRunning(Task<E> runningTask, Task<E> reporter) {
        running();
    }

    @Override
    public void childFail(Task<E> runningTask) {
        fail();
    }

    @Override
    public void childSuccess(Task<E> runningTask) {
        success();
    }

    /**
     * This method should be called when game entity needs to make decisions: call this in game loop or after a fixed
     * time slice if the game is real-time, or on entity's turn if the game is turn-based
     * 当游戏实体需要做出决策时，应该调用此方法:如果游戏是实时的，则在游戏循环或固定时间片之后调用此方法;如果游戏是基于回合的，则在实体回合调用
     */
    public void step() {
        if (rootTask.status == Status.RUNNING) {
            rootTask.run();
        } else {
            rootTask.setControl(this);
            rootTask.start();
            if (rootTask.checkGuard(this)) {
                rootTask.run();
            } else {
                rootTask.fail();
            }
        }
    }

    /**
     * 通知添加子任务
     *
     * @param task
     * @param index
     * @author JiangZhiYong
     * @QQ 359135103 2017年11月22日 下午4:00:00
     */
    public void notifyChildAdded(Task<E> task, int index) {
        // 遍历所有监听器
        for (Listener<E> listener : listeners) {
            listener.childAdded(task, index);
        }
    }

    /**
     * 通知任务更新
     *
     * @param task
     * @param previousStatus
     * @author JiangZhiYong
     * @QQ 359135103 2017年11月22日 下午5:04:00
     */
    public void notifyStatusUpdated(Task<E> task, Status previousStatus) {
        for (Listener<E> listener : listeners) {
            listener.statusUpdated(task, previousStatus);
        }
    }

    @Override
    public void release() {
        removeListeners();
        this.rootTask = null;
        this.object = null;
        this.listeners = null;
        super.release();
    }

    @Override
    public void run() {
    }

    @Override
    public void resetTask() {
        super.resetTask();
        tree = this;
    }

    /**
     * 添加监听器
     *
     * @param listener
     * @author JiangZhiYong
     * @QQ 359135103 2017年11月22日 下午5:39:57
     */
    public void addListener(Listener<E> listener) {
        if (listeners == null) {
            listeners = new ArrayList<Listener<E>>();
        }
        listeners.add(listener);
    }

    public void removeListener(Listener<E> listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    public void removeListeners() {
        if (listeners != null) {
            listeners.clear();
        }
    }

    /**
     * 行为树事件
     *
     * @param <E>
     * @author JiangZhiYong
     * @QQ 359135103 2017年11月22日 下午3:50:50
     */
    public interface Listener<E> {

        /**
         * 状态更新
         *
         * @param task
         * @param previousStatus 之前状态
         * @author JiangZhiYong
         * @QQ 359135103 2017年11月22日 下午3:52:23
         */
        default void statusUpdated(Task<E> task, Status previousStatus) {

        }

        /**
         * 添加子任务
         *
         * @param task  子任务
         * @param index 子任务位置
         * @author JiangZhiYong
         * @QQ 359135103 2017年11月22日 下午3:55:20
         */
        default void childAdded(Task<E> task, int index) {

        }

    }

    /**
     * 安全监测
     *
     * @param <E>
     */
    private static final class GuardEvaluator<E> extends Task<E> {

        // No argument constructor useful for Kryo serialization
        @SuppressWarnings("unused")
        public GuardEvaluator() {
        }

        public GuardEvaluator(BehaviorTree<E> tree) {
            this.tree = tree;
        }

        @Override
        protected int addChildToTask(Task<E> child) {
            return 0;
        }

        @Override
        public int getChildCount() {
            return 0;
        }

        @Override
        public Task<E> getChild(int i) {
            return null;
        }

        @Override
        public void run() {
        }

        @Override
        public void childSuccess(Task<E> task) {
        }

        @Override
        public void childFail(Task<E> task) {
        }

        @Override
        public void childRunning(Task<E> runningTask, Task<E> reporter) {
        }

    }

}
