import java.util.ArrayList;
import java.util.List;

public class AVLTree {
    private static class Node {
        int key;
        Node left, right;
        int height; // высота узла

        Node(int k) { // конструктор класса
            key = k;
            height = 1;
            left = right = null;
        }
    }

    private Node root; // корень
    private int rotations = 0; // повороты
    private int nodeCount = 0; // кол-во узлов

    public int getRotations(){return rotations;}
    public void resetRotations() {this.rotations = 0;}
    public int getNodeCount() {return nodeCount;}

    public boolean contains(int key) {
        return search(root, key) != null;
    }

    private Node search(Node node, int key) {
        while (node != null) {
            if (key == node.key) return node;
            node = key < node.key ? node.left : node.right; // идем влево или вправо, пока ключ не совпадет
        }
        return null;
    }

    public void insert(int key) { // рекурсивная вставка
        root = insertRec(root, key);
    }

    private Node insertRec(Node node, int key) {
        if (node == null) { // нашли место для нового узла
            nodeCount++;
            return new Node(key); // создаем новый узел
        }

        if (key < node.key) { // рекурсивный поиск места для вставки
            node.left = insertRec(node.left, key); // ключ меньше идем влево, иначе вправо
        } else {
            node.right = insertRec(node.right, key);
        }

        updateHeight(node); // обновляем высоту текущего узла
        return balance(node); // баланс+возврат результата
    }

    public void delete(int key) {
        root = deleteRec(root, key); // рекурсивн удаление
    }

    private Node deleteRec(Node node, int key) {
        if (node == null) return null;

        Node result; //  переменная для хранения результата, который будем балансировать

        if (key < node.key) { // ключ меньше - идем влево
            node.left = deleteRec(node.left, key);
            result = node; // сохраняем текущий узел как результат (после удаления в левом поддереве)
        } else if (key > node.key) { // ключ больше - идем вправо
            node.right = deleteRec(node.right, key);
            result = node;
        } else { // нашли узел для удаления (key == node.key)
            if (node.left == null) { // СЛ1 - у узла нет левого потомка (0 или 1 потомок)
                nodeCount--;
                result = node.right; // возвращаем правого потомка
            } else if (node.right == null) { // СЛ2 - у узла нет правого потомка
                nodeCount--;
                result = node.left; // левого
            } else { // СЛ 3 - у узла есть оба потомка
                Node preemnik = minValue(node.right); // минимальный узел в правом поддереве
                node.key = preemnik.key; // заменяем ключ текущего узла на ключ преемника
                node.right = deleteRec(node.right, preemnik.key); // рекурсивно удаляем преемника из правого поддерева
                result = node;
            }
        }

        if (result != null) { // также балансируем каждый возвращаемый узел
            updateHeight(result);
            result = balance(result);
        }

        return result;
    }

    private Node minValue(Node node) { // поиск мин. узла в поддереве(влево до упора)
        while (node.left != null) node = node.left;
        return node;
    }

    private Node balance(Node node) { // балансировка
        int bf = balanceFactor(node); //  balance factor - разница между высотой левого и правого поддерева узла
        if (bf > 1) { // СЛ 1: левое поддерево выше на 2 и более
            if (balanceFactor(node.left) < 0) { // подслучай 1B - left-right случай (LR)
                node.left = rotateLeft(node.left);
            };
            return rotateRight(node); // подслучай 1A - left-left
        } else if (bf < -1) { // СЛ 2: правое поддерево выше на 2 и более
            if (balanceFactor(node.right) > 0) {  //  подслучай 2B: right-left (зигзаг)
                node.right = rotateRight(node.right); // правый поворот правого потомка
            };
            return rotateLeft(node); // подслучай 2A - right-right
        }
        return node;
    }

    private int height(Node n) { // возвращает значение, которое уже записано внутри узла в поле height
        return n == null ? 0 : n.height; // высота null = 0, иначе берем из узла
    }

    private void updateHeight(Node n) { // хранение высоты, чтобы потом быстро её использовать
        n.height = 1 + Math.max(height(n.left), height(n.right)); // высота = 1 + максимум из высот потомков
    }

    private int balanceFactor(Node n) {
        return n == null ? 0 : height(n.left) - height(n.right); // BF = высота(лево) - высота(право)
    }

    private Node rotateRight(Node y) {
        Node x = y.left; // x - левый потомок y (станет новым корнем)
        Node T2 = x.right; // T2 - правое поддерево x

        x.right = y; // поворот - y становится правым потомком x
        y.left = T2; // T2 становится левым потомком y

        updateHeight(y);
        updateHeight(x);
        rotations++;

        return x;
    }

    private Node rotateLeft(Node x) {
        Node y = x.right; // y - правый потомок x (станет новым корнем)
        Node T2 = y.left; // T2 - левое поддерево y

        y.left = x; // поворот -  x становится левым потомком y
        x.right = T2; // T2 становится правым потомком x

        updateHeight(x);
        updateHeight(y);
        rotations++;

        return y;
    }

    public int getHeight() {return root == null ? 0 : root.height;} // получение высоты

    static class AVLNodeWrapper { // класс-обертка для передачи данных в TreeCanvas
        int key;
        AVLNodeWrapper left, right;
        int depth;
        int balanceFactor;

        AVLNodeWrapper(int k, int depth, int balanceFactor){
            this.key = k;
            this.depth = depth;
            this.balanceFactor = balanceFactor;
        }
    }

    public AVLNodeWrapper getRootWrapper() {
        if (root == null) return null;
        return buildWrapper(root, 0); // построение с корня (глубина 0)
    }

    private AVLNodeWrapper buildWrapper(Node node, int depth) { // рекурсивное построение дерева для отрисовки
        if (node == null) return null;

        int bf = balanceFactor(node); // вычисляем balance factor для этого узла

        AVLNodeWrapper w = new AVLNodeWrapper(node.key, depth, bf); // создаем обертку с данными для отрисовки

        w.left = buildWrapper(node.left, depth+1);
        w.right = buildWrapper(node.right, depth+1);
        return w;
    }

    public List<Integer> findPath(int key) {
        List<Integer> path = new ArrayList<>();
        findPathRec(root, key, path); // рекурсивный поиск с записью пути
        return path;
    }

    private boolean findPathRec(Node node, int key, List<Integer> path) {
        if (node == null) return false;

        path.add(node.key); // добавляе текущий узел в путь

        if (key == node.key) {
            return true;
        } else if (key < node.key) {
            if (findPathRec(node.left, key, path)) return true; // ищем в левом/правом поддереве
        } else {
            if (findPathRec(node.right, key, path)) return true;
        }

        path.remove(path.size() - 1); // убираем из пути если не нашли в этом поддереве
        return false;
    }
}