package pl.guz.rxjavaexamples.lists;

import io.reactivex.Flowable;
import io.reactivex.flowables.GroupedFlowable;
import io.reactivex.functions.BiFunction;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class UnevenLists {

    private static Logger logger = LoggerFactory.getLogger(UnevenLists.class);

    @Test
    public void join_uneven_lists() {
        List<User> userList = Arrays.asList(
                new User(2, "B"),
                new User(1, "A"),
                new User(4, "D")
        );

        List<Order> orderList = Arrays.asList(
                new Order(3, "OC1"),
                new Order(1, "OA1"),
                new Order(1, "OA2"),
                new Order(2, "OB1")
        );


        leftJoin(
                Flowable.fromIterable(userList)
                        .doOnSubscribe(x -> logger.info("Subscribe userlist"))
                        .doOnNext(x -> logger.info("user list emit: {}", x))
                ,
                User::id,
                Flowable.fromIterable(orderList)
                        .doOnSubscribe(x -> logger.info("Subscribe orderlist"))
                        .doOnNext(x -> logger.info("order list emit: {}", x))
                ,
                Order::userId,
                UserWithOrders::new
        )
                .test()
                .assertComplete()
                .assertValues(
                        new UserWithOrders(new User(2, "B"), Arrays.asList(new Order(2, "OB1"))),
                        new UserWithOrders(new User(1, "A"), Arrays.asList(new Order(1, "OA1"), new Order(1, "OA2"))),
                        new UserWithOrders(new User(4, "D"), Arrays.asList())
                );
    }

    private <R, L, Key, V> Flowable<V> leftJoin(Flowable<R> right, Function<R, Key> groupByKeyRigth, Flowable<L> left, Function<L, Key> groupByKeyLeft, BiFunction<R, List<L>, V> zipFunction) {
        Flowable<GroupedFlowable<Key, R>> rigthGroupedByKey = right.groupBy(groupByKeyRigth::apply);
        Flowable<GroupedFlowable<Key, L>> leftGroupedByKey = left.groupBy(groupByKeyLeft::apply);
        return leftGroupedByKey.publish(leftGroupedByKeyStream ->
                rigthGroupedByKey
                        .flatMap(rigthGrouped -> leftGroupedByKeyStream
                                .doOnNext(leftGrouped -> logger.info("Search rKey: {} == lKey: {}", rigthGrouped.getKey(), leftGrouped.getKey()))
                                .filter(leftGrouped -> compareKey(rigthGrouped, leftGrouped))
                                .doOnNext(leftGrouped -> logger.info("Found rKey: {} == lKey: {}", rigthGrouped.getKey(), leftGrouped.getKey()))
                                .flatMap(leftGroupedFlowable -> leftGroupedFlowable)
                                .toList()
                                .zipWith(rigthGrouped.singleOrError(), (List<L> listL, R r) -> zipFunction.apply(r, listL))
                                .toFlowable()
                        )
        );
    }

    private <R, L, Key> boolean compareKey(GroupedFlowable<Key, R> rigthGrouped, GroupedFlowable<Key, L> leftGrouped) {
        return leftGrouped.getKey().equals(rigthGrouped.getKey());
    }
}


class User {
    private final Integer id;
    private final String name;

    User(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    Integer id() {
        return id;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) &&
                Objects.equals(name, user.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}

class Order {
    private final Integer userId;
    private final String name;

    Order(Integer userId, String name) {
        this.userId = userId;
        this.name = name;
    }


    Integer userId() {
        return userId;
    }

    @Override
    public String toString() {
        return "Order{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(userId, order.userId) &&
                Objects.equals(name, order.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, name);
    }
}

class UserWithOrders {
    private final User user;
    private final List<Order> orders;

    UserWithOrders(User user, List<Order> orders) {
        this.user = user;
        this.orders = orders;
    }

    @Override
    public String toString() {
        return "UserWithOrders{" +
                "user=" + user +
                ", orders=" + orders +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserWithOrders that = (UserWithOrders) o;
        return Objects.equals(user, that.user) &&
                Arrays.deepEquals(orders.toArray(), that.orders.toArray());
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, orders);
    }
}
