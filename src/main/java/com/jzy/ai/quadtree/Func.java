package com.jzy.ai.quadtree;


import com.jzy.ai.nav.polygon.Polygon;
import com.jzy.ai.quadtree.polygon.PolygonGuadTree;

/**
 * 功能函数
 *
 * @param <V>
 * @author JiangZhiYong
 * @mail 359135103@qq.com
 */
public interface Func<V> {
//	public default void call(PointQuadTree<V> quadTree, Node<V> node) {
//		
//	}

    public default void call(PolygonGuadTree quadTree, Node<Polygon> node) {

    }
}
