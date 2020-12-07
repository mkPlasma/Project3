package main;

import java.util.Objects;

public class ColliderInterval {
    float interval;
    Collider collider;
    boolean start;

    public ColliderInterval(float _interval, Collider _collider, boolean _start){
        interval = _interval;
        collider = _collider;
        start = _start;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ColliderInterval)) return false;
        ColliderInterval that = (ColliderInterval) o;
        return Float.compare(that.interval, interval) == 0 &&
                start == that.start &&
                collider.equals(that.collider);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interval, collider, start);
    }
}
