import java.util.ArrayList;
import java.util.List;

public class RedBlackTree {

    static final boolean RED = true;
    static final boolean BLACK = false;

    private static class Node {
        int key; // значение узла
        Node left, right, parent; // потомки и родитель
        boolean color;


        Node(int k) {
            key = k;
            color = RED; // новые узлы всегда красные
            left = right = parent = null; // изначально нет связей
        }
    }

    // nil лист
    private final Node NIL = new Node(0);
    private Node root = NIL;
    private int rotations = 0;
    private int nodeCount = 0;

    public RedBlackTree() { // nil всегда черные и тд
        NIL.color = BLACK;
        NIL.left = NIL.right = NIL.parent = NIL;
        root = NIL;
    }

    public int getRotations() {return rotations;}
    public void resetRotations() {this.rotations = 0;}
    public int getNodeCount() {return nodeCount;}


    public int getHeight() { // рекурсивно ищем узел
        return heightHelper(root);
    }

    private int heightHelper(Node node) {
        if (node == NIL) return 0;
        return 1 + Math.max(heightHelper(node.left), heightHelper(node.right)); // высота = 1 + максимум из высот поддеревьев
    }

    public boolean contains(int key) {
        return searchNode(root, key) != NIL;
    }

    private Node searchNode(Node x, int key) { // обычный бинарный поиск
        while (x != NIL) { // пока не дошли до nil`а
            if (key == x.key) return x;
            x = (key < x.key) ? x.left : x.right; // идем влево если key меньше, вправо если больше
        }
        return NIL;
    }

    public void insert(int key) {
        Node z = new Node(key); // создаем новый узел с заданным ключом
        z.left = z.right = z.parent = NIL; // изначально связи ведут в NIL

        Node y = NIL; // потенциальный родитель
        Node x = root; // текущ вершина(рут)
        while (x != NIL) {
            y = x; // запоминаем текущий узел как потенциального родителя
            if (z.key < x.key) {
                x = x.left; // ключ меньше - идем в ЛЕВОЕ поддерево, и наоборот
            } else {
                x = x.right;
            }
        }

        z.parent = y; // назначаем найденного родителя новому узлу
        if (y == NIL) root = z; // особый случай: дерево было пустым - новый узел становится корнем
        else if (z.key < y.key) y.left = z; // новый узел становится левым потомком, иначе правым
        else y.right = z;

        z.color = RED; // новые узлы всегда красные
        insertFixup(z); // балансируем
        nodeCount++;
    }

    private void insertFixup(Node z) {
        while (z.parent != NIL && z.parent.color == RED) { // два красных узла подряд - нарушение
            if (z.parent == z.parent.parent.left) { // 1СЛ родитель - ЛЕВЫЙ потомок дедушки
                Node y = z.parent.parent.right; // дядя - брат родителя
                if (y != NIL && y.color == RED) { // подслучай A1 - дядя красный
                    z.parent.color = BLACK; // родитель - черный
                    y.color = BLACK; // дядя - черный
                    z.parent.parent.color = RED; // дедушка красный
                    z = z.parent.parent; // переход к деду
                } else { // подслучай B1: дядя черный
                    if (z == z.parent.right) { // если z - правый потомок, то поднимаемся к родителю и делаем левый поворот
                        z = z.parent;
                        leftRotate(z);
                    }
                    z.parent.color = BLACK; // теперь z - левый потомок
                    z.parent.parent.color = RED; // тогда дедушка становится красным и правый поворот
                    rightRotate(z.parent.parent);
                }
            } else { // 2СЛ родитель - ПРАВЫЙ потомок дедушки (симметрично 1сл)
                Node y = z.parent.parent.left;
                if (y != NIL && y.color == RED) { // подслучай А2 - дядя красный
                    z.parent.color = BLACK;
                    y.color = BLACK;
                    z.parent.parent.color = RED;
                    z = z.parent.parent;
                } else { // подслучай B2 - дядя черный
                    if (z == z.parent.left) {
                        z = z.parent;
                        rightRotate(z);
                    }
                    z.parent.color = BLACK;
                    z.parent.parent.color = RED;
                    leftRotate(z.parent.parent);
                }
            }
        }
        root.color = BLACK; // ну и корень всегда черный
    }

    public void delete(int key) {
        Node z = searchNode(root, key);  // ищем узел для удалния
        if (z == NIL) return;

        Node y = z; //  узел, который удаляем из дерева
        boolean yOriginalColor = y.color;
        Node x; // узел котороый займет место y

        if (z.left == NIL) { // 1СЛ - у z нет левого потомка, тогда правый потомок займет место
            x = z.right;
            transplant(z, z.right);
        } else if (z.right == NIL) { // 2СЛ - у z нет правого потомка, тогда левый потомок займет место
            x = z.left;
            transplant(z, z.left);
        } else { // 3СЛ - у z два потомка
            y = minimum(z.right); // преемник(минимальный в правом поддереве)
            yOriginalColor = y.color;
            x = y.right; // потомок преемника

            if (y.parent == z) { // преемник - непосредственный правый потомок z
                x.parent = y;
            } else { // преемник глубже в правом поддереве
                transplant(y, y.right); // вырезаем y из его текущей позиции
                y.right = z.right; // правое поддерево z становится правым поддеревом y
                y.right.parent = y; // обновляем родителя
            }
            transplant(z, y); // меняем z на y в дереве
            y.left = z.left; //левое поддерево z становится левым поддеревом y
            y.left.parent = y; // обновляем родителя и затем сохраняем цвет z
            y.color = z.color;
        }
        if (yOriginalColor == BLACK) deleteFixup(x); // балансировка черной высоты в случае удаления черного узла
        nodeCount--;
    }

    private void transplant(Node u, Node v) { // замена поддерева с корнем u на другое с v
        if (u.parent == NIL) root = v; // u был корнем
        else if (u == u.parent.left) u.parent.left = v; // u был левым потомком
        else u.parent.right = v; // u был правым потомком
        v.parent = u.parent; // обновление родителя
    }

    private Node minimum(Node x) { // возвращает минимальный узел (самый левый) в поддереве x
        while (x.left != NIL) x = x.left;
        return x;
    }

    private void deleteFixup(Node x) { // удалили черный узел - нарушение черной высоты
        while (x != root && x.color == BLACK) { // пока x не корень И x черный
            if (x == x.parent.left) { // 1 СЛ: x - левый потомок
                Node w = x.parent.right; // "брат" x
                if (w.color == RED) { // 1А: брат красный
                    w.color = BLACK; // брата в черный
                    x.parent.color = RED; // родителей в красный
                    leftRotate(x.parent); // левый поворот
                    w = x.parent.right; // обновление ссылки на брата и можем применять случаи 1B/1C/1D
                }
                if ((w.left == NIL || w.left.color == BLACK) && // сл 1B: оба потомка брата ЧЕРНЫЕ
                        (w.right == NIL || w.right.color == BLACK)) {
                    w.color = RED; // брат становится красным
                    x = x.parent; // поднимаем проблему на уровень выше
                } else { // случай 1С: правый потомок брата ЧЕРНЫЙ
                    if (w.right == NIL || w.right.color == BLACK) {
                        if (w.left != NIL) w.left.color = BLACK; // левый потомк w - черный
                        w.color = RED; // брат становится красным, правый поворот брата,
                        rightRotate(w);
                        w = x.parent.right; // обновляем брата
                    } // случай 1D: правый потомок брата КРАСНЫЙ
                    w.color = x.parent.color; // брат наследует цвет родителя
                    x.parent.color = BLACK; // родитель становится черным
                    if (w.right != NIL) w.right.color = BLACK; // правый потомок брата черный
                    leftRotate(x.parent); // левый поворот родители
                    x = root;
                }
            } else { //   2 СЛ: x - ПРАВЫЙ потомок (симметрично cлучаю 1)
                Node w = x.parent.left;
                if (w.color == RED) {
                    w.color = BLACK;
                    x.parent.color = RED;
                    rightRotate(x.parent);
                    w = x.parent.left;
                }
                if ((w.right == NIL || w.right.color == BLACK) &&
                        (w.left == NIL || w.left.color == BLACK)) {
                    w.color = RED;
                    x = x.parent;
                } else {
                    if (w.left == NIL || w.left.color == BLACK) {
                        if (w.right != NIL) w.right.color = BLACK;
                        w.color = RED;
                        leftRotate(w);
                        w = x.parent.left;
                    }
                    w.color = x.parent.color;
                    x.parent.color = BLACK;
                    if (w.left != NIL) w.left.color = BLACK;
                    rightRotate(x.parent);
                    x = root;
                }
            }
        }
        x.color = BLACK;
    }

    private void leftRotate(Node x) {
        Node y = x.right;   //  y - правый потомок x (станет новым корнем)

        // перемещаем левое поддерево y к x
        x.right = y.left;   // левое поддерево y становится правым поддеревом x
        if (y.left != NIL) {
            y.left.parent = x; // обновляем родителя для левого поддерева y
        }

        // перемещаем родительскую связь
        y.parent = x.parent; // y наследует родителя x

        if (x.parent == NIL) {
            root = y;          // x был корнем - y становится новым корнем
        } else if (x == x.parent.left) {
            x.parent.left = y; // x был левым потомком - y занимает его место
        } else {
            x.parent.right = y;// x был правым потомком - y занимает его место
        }

        // делаем x левым потомком y
        y.left = x; // x становится левым потомком y
        x.parent = y; // обновляем родителя x

        rotations++;
    }

    private void rightRotate(Node x) { // правый поворот вокруг узла x
        Node y = x.left; // y - левый потомок x (станет новым корнем)

        // перемещаем правое поддерево y к x
        x.left = y.right; // правое поддерево y становится левым поддеревом x
        if (y.right != NIL) {
            y.right.parent = x; // обновляем родителя для правого поддерева y
        }

        // перемещаем родительскую связь
        y.parent = x.parent; // y наследует родителя x

        if (x.parent == NIL) {
            root = y;          // x был корнем → y становится новым корнем
        } else if (x == x.parent.right) {
            x.parent.right = y; // x был правым потомком → y занимает его место
        } else {
            x.parent.left = y;  // x был левым потомком → y занимает его место
        }

        // делаем x правым потомком y
        y.right = x;  // x становится правым потомком y
        x.parent = y;   // обновляем родителя x

        rotations++;  // увеличиваем счетчик поворотов
    }

    static class RBTNodeWrapper { // класс-обертка для отрисовки
        int key; // значение узла
        boolean isRed;
        RBTNodeWrapper left, right; // упрощенные связи (только для отрисовки)
        int depth;
        RBTNodeWrapper(int k, boolean isRed, int depth) {
            this.key = k;
            this.isRed = isRed;
            this.depth = depth;
        }
    }

    public RBTNodeWrapper getRootWrapper() { // получение корня для визуализации
        if (root == NIL) return null;
        return buildWrapper(root, 0); // запуск рекурсии построение с корня (глубина = 0)
    }

    private RBTNodeWrapper buildWrapper(Node node, int depth) { // рекурсивное построение дерева для отрисовки
        if (node == NIL) return null; // нилы не рисуем

        RBTNodeWrapper w = new RBTNodeWrapper( // обертка текущего узла
                node.key,
                node.color == RED,
                depth);
        w.left = buildWrapper(node.left, depth+1); // рекурсивно строим обертки для потомков
        w.right = buildWrapper(node.right, depth+1);
        return w;
    }

    public List<Integer> findPath(int key) {
        List<Integer> path = new ArrayList<>();
        findPathRec(root, key, path); // запускаем рекурсивный поиск с записью пути
        return path;
    }

    private boolean findPathRec(Node node, int key, List<Integer> path) {
        if (node == NIL) return false; // дошли до листа - целевой ключ не найде

        path.add(node.key); // запись текущего узла в путь

        if (key == node.key) {
            return true;
        } else if (key < node.key) { // ищем в левом/правом поддереве
            if (findPathRec(node.left, key, path)) return true;
        } else {
            if (findPathRec(node.right, key, path)) return true;
        }

        path.remove(path.size() - 1); // убираем из пути если не нашли в этом поддереве
        return false;
    }
}
