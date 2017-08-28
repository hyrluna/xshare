package xh.xshare.widget;

import android.graphics.Point;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import static android.support.v7.widget.RecyclerView.NO_POSITION;

/**
 * Created by G1494458 on 2017/6/17.
 */

public class CustomLayoutManager extends RecyclerView.LayoutManager {

    public int decoratedWidth;
    public int decoratedHeight;
    public int halfWidth;
    public int halfHeight;
    public Point recyclerCenter;
    private int currentPosition;
    private int horizontalScrollOffset;
    private int totalWidth;
    private int scrollToChangeCurrent;

    //表示在单个child 已经滚动的距离
    private int scrolled;
    //手指放开之后即将滑动的距离，回到平衡位置
    private int pendingScroll;

    public CustomLayoutManager() {
        recyclerCenter = new Point();
        this.currentPosition = NO_POSITION;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        //如果adapter中数据集合为0，那么回收所有child views
        if (state.getItemCount() == 0) {
            removeAndRecycleAllViews(recycler);
            currentPosition = NO_POSITION;
            return;
        }

        //如果adapter中数据集合不为0, 那么下面根据adapter中的数据进行相应的布局
        if (getChildCount() == 0) {
            //这里假设所有的child view的尺寸相同，在这里我们获取一些常量，以便之后使用
            View child = recycler.getViewForPosition(0);
            addView(child);
            measureChildWithMargins(child, 0, 0);
            decoratedWidth = getDecoratedMeasuredWidth(child);
            decoratedHeight = getDecoratedMeasuredHeight(child);
            halfWidth = decoratedWidth / 2;
            halfHeight = decoratedHeight / 2;
            scrollToChangeCurrent = decoratedWidth;
            recyclerCenter.set(getWidth()/2, getHeight()/2);
        }

        //在对child views进行布局之前，先将现有已经attach了的view分离，然后全部重新布局
        detachAndScrapAttachedViews(recycler);


        totalWidth = 0;
        for (int i = 0; i < getItemCount(); i++) {
            //从recycler中获取view，由于recycler的特性，会从scrap heap或recycler pool中获取view
            View v = recycler.getViewForPosition(i);

            //下面是布局的主要步骤，先将child view添加到recycler view，然后再测量，最后布局
            addView(v);
            measureChildWithMargins(v, 0, 0);
            //left, top, right, bottom
            //在這裡可以修改item view的佈局方式
            if (i % 2 == 0) {
                layoutDecoratedWithMargins(v,
                        recyclerCenter.x - halfWidth + decoratedWidth*i, recyclerCenter.y - halfHeight - 100,
                        recyclerCenter.x + halfWidth + decoratedWidth*i, recyclerCenter.y + halfHeight - 100);
            } else {
                layoutDecoratedWithMargins(v,
                        recyclerCenter.x - halfWidth + decoratedWidth*i, recyclerCenter.y - halfHeight + 100,
                        recyclerCenter.x + halfWidth + decoratedWidth*i, recyclerCenter.y + halfHeight + 100);
            }

            totalWidth += decoratedWidth;
        }

    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public void onItemsAdded(RecyclerView recyclerView, int positionStart, int itemCount) {
        if (currentPosition == NO_POSITION) {
            currentPosition = 0;
        } else if (currentPosition >= positionStart) {
            currentPosition = Math.min(currentPosition + itemCount, getItemCount() - 1);
        }
    }

    //distance to scroll by in pixels. X increases as scroll position approaches the right.
    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getChildCount() == 0) {
            return 0;
        }

//        // delta > 0 ? END : START;如果recycler view向上滚动，那么layout manager需要向下布局新的child view
//        Direction direction = Direction.fromDelta(dx);
//        //剩余的滚动距离
//        int leftToScroll = calculateAllowedScrollIn(direction);
//        if (leftToScroll <= 0) {
//            return 0;
//        }
//
//        //applyTo end 不变，start 为 -1
//        int delta = direction.applyTo(Math.min(leftToScroll, Math.abs(dx)));
//        scrolled += delta;
//        if (pendingScroll != 0) {
//            //准备回弹的距离
//            pendingScroll -= delta;
//        }


        //滚动位置越靠近recyclerview右侧，dx增加
        int travel = dx;
        Log.d("test", "scrollHorizontallyBy: " + dx);
        if (horizontalScrollOffset + dx < 0) {
            //到达最左边
            travel = -horizontalScrollOffset;
        } else if (horizontalScrollOffset + dx > totalWidth - decoratedWidth) {
            //实际滚动距离超出最大可以滚动的距离
            travel = totalWidth - decoratedWidth - horizontalScrollOffset;
        }
        horizontalScrollOffset += travel;
        offsetChildrenHorizontal(-travel);
        return travel;
    }

//    private int calculateAllowedScrollIn(Direction direction) {
//        if (pendingScroll != 0) {
//            return Math.abs(pendingScroll);
//        }
//        int allowedScroll;
//        boolean isBoundReached;
//        //大于0向右，小于0向左
//        boolean isScrollDirectionAsBefore = direction.applyTo(scrolled) > 0;
//        if (direction == Direction.START && currentPosition == 0) {
//            //We can scroll to the left when currentPosition == 0 only if we scrolled to the right before
//            //当currentPosition = 0时我们可以向左滑动，一直滑动到最右边。
//            isBoundReached = scrolled == 0;
//            allowedScroll = isBoundReached ? 0 : Math.abs(scrolled);
//        } else if (direction == Direction.END && currentPosition == getItemCount() - 1) {
//            //We can scroll to the right when currentPosition == last only if we scrolled to the left before
//            isBoundReached = scrolled == 0;
//            allowedScroll = isBoundReached ? 0 : Math.abs(scrolled);
//        } else {
//            isBoundReached = false;
//            allowedScroll = isScrollDirectionAsBefore ?
//                    scrollToChangeCurrent - Math.abs(scrolled) :
//                    scrollToChangeCurrent + Math.abs(scrolled);
//        }
////        scrollStateListener.onIsBoundReachedFlagChange(isBoundReached);
//        return allowedScroll;
//    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
    }

    private boolean isAnotherItemCloserThanCurrent() {
        //已经滚动的距离大于一半
        return Math.abs(scrolled) >= scrollToChangeCurrent * 0.6f;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        return super.scrollVerticallyBy(dy, recycler, state);
    }

    //    @Override
//    public void onScrollStateChanged(int state) {
//        if (currentScrollState == RecyclerView.SCROLL_STATE_IDLE && currentScrollState != state) {
//            scrollStateListener.onScrollStart();
//        }
//
//        if (state == RecyclerView.SCROLL_STATE_IDLE) {
//            //Scroll is not finished until current view is centered
//            boolean isScrollEnded = onScrollEnd();
//            if (isScrollEnded) {
//                scrollStateListener.onScrollEnd();
//            } else {
//                //Scroll continues and we don't want to set currentScrollState to STATE_IDLE,
//                //because this will then trigger .scrollStateListener.onScrollStart()
//                return;
//            }
//        } else if (state == RecyclerView.SCROLL_STATE_DRAGGING) {
//            onDragStart();
//        }
//        currentScrollState = state;
//    }

    public int getHorizontalSpace() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }
}
