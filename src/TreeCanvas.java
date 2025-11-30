import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.scene.control.Alert;
import java.util.*;

public class TreeCanvas extends Canvas {

    private static final double NODE_RADIUS = 18; // радиус кружка
    private static final double LEVEL_HEIGHT = 70; // расстояние м/у уровнями дерева

    private RedBlackTree rbt;
    private AVLTree avl;
    private boolean isRbt;

    public TreeCanvas(boolean isRbt) { // конструктор
        super(1000, 600);
        this.isRbt = isRbt;
    }

    public void setTreeData(RedBlackTree rbt) { // пришло RBT - рисуем РБТ
        this.rbt = rbt;
        this.avl = null;
        draw();
    }

    public void setTreeData(AVLTree avl) {
        this.avl = avl;
        this.rbt = null;
        draw();
    }

    private void draw() { // основной метод отрисовки
        GraphicsContext g = getGraphicsContext2D(); // объект для рисования
        double w = getWidth();
        double h = getHeight();
        g.clearRect(0,0,w,h);

        try {
            if (isRbt) { // ветка КЧД
                if (rbt == null) return;
                RedBlackTree.RBTNodeWrapper root = rbt.getRootWrapper();
                if (root == null) return;
                Map<RedBlackTree.RBTNodeWrapper, Integer> xMap = new HashMap<>(); // карта для хранения X-коорд. каждого узла
                int[] x = {0}; // массив из одного элемента для передачи по ссылке
                inorderAssignX(root, xMap, x); // inorder обходом назначаем X-координаты узлам
                double colWidth = w / Math.max(1, x[0] + 1); // ширина одной "колонки" для узла.
                drawEdgesRBT(g, root, xMap, colWidth); // связи между узлами
                drawNodesRBT(g, root, xMap, colWidth); // сами узлы
            } else {
                if (avl == null) return; // avl дерево - аналогично
                AVLTree.AVLNodeWrapper root = avl.getRootWrapper();
                if (root == null) return;
                Map<AVLTree.AVLNodeWrapper, Integer> xMap = new HashMap<>();
                int[] x = {0};
                inorderAssignXAVL(root, xMap, x);
                double colWidth = w / Math.max(1, x[0] + 1);
                drawEdgesAVL(g, root, xMap, colWidth);
                drawNodesAVL(g, root, xMap, colWidth);
            }
        } catch (Exception e) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setHeaderText("Ошибка при рисовании");
            a.setContentText(e.getMessage());
            a.showAndWait();
        }
    }

    // методы для RBT
    private void inorderAssignX(RedBlackTree.RBTNodeWrapper node,
                                Map<RedBlackTree.RBTNodeWrapper,Integer> xMap,
                                int[] xCounter) { // назначение x-индексов
        if (node == null) return;
        inorderAssignX(node.left, xMap, xCounter); // сначала обрабатываем левое поддерево; все узлы левого поддерева получат меньшие номера

        xMap.put(node, xCounter[0]++); // текущему узлу даем следующий X
        inorderAssignX(node.right, xMap, xCounter); // теперь с правым
    }

    private void drawEdgesRBT(GraphicsContext g, // метод рисования связец
                              RedBlackTree.RBTNodeWrapper node,
                              Map<RedBlackTree.RBTNodeWrapper,Integer> xMap,
                              double colWidth) {
        if (node == null) return;
        double x = (xMap.get(node)+0.5) * colWidth; // +0.5 чтобы узел был посередине колонки, а не у края
        double y = 30 + node.depth * LEVEL_HEIGHT;

        if (node.left != null) { // линия к левому узлу
            double lx = (xMap.get(node.left)+0.5) * colWidth; //x,y левого
            double ly = 30 + node.left.depth * LEVEL_HEIGHT;
            if (isPointInBounds(x, y) && isPointInBounds(lx, ly)) { // проверка границы для линий
                g.setStroke(Color.GRAY); // оба конца линии должны быть в пределах canvas
                g.strokeLine(x, y, lx, ly);
            }
            drawEdgesRBT(g, node.left, xMap, colWidth);
        }
        if (node.right != null) { // аналогично для правого потомка
            double rx = (xMap.get(node.right)+0.5) * colWidth;
            double ry = 30 + node.right.depth * LEVEL_HEIGHT;
            if (isPointInBounds(x, y) && isPointInBounds(rx, ry)) {
                g.setStroke(Color.GRAY);
                g.strokeLine(x, y, rx, ry);
            }
            drawEdgesRBT(g, node.right, xMap, colWidth);
        }
    }

    private void drawNodesRBT(GraphicsContext g, // рисование узлоа
                              RedBlackTree.RBTNodeWrapper node,
                              Map<RedBlackTree.RBTNodeWrapper,Integer> xMap,
                              double colWidth) {
        if (node == null) return;
        drawNodesRBT(g, node.left, xMap, colWidth);

        double x = (xMap.get(node)+0.5) * colWidth; // x y коорды
        double y = 30 + node.depth * LEVEL_HEIGHT;

        if (!isPointInBounds(x, y)) { // проверка на выход из границы
            drawNodesRBT(g, node.right, xMap, colWidth);
            return;
        }

        g.setFill(node.isRed ? Color.RED : Color.BLACK);
        g.fillOval(x - NODE_RADIUS, y - NODE_RADIUS, NODE_RADIUS*2, NODE_RADIUS*2); // круг
        g.setFill(Color.WHITE);
        g.setTextAlign(TextAlignment.CENTER);
        g.setTextBaseline(VPos.CENTER);
        g.fillText(String.valueOf(node.key), x, y);

        drawNodesRBT(g, node.right, xMap, colWidth);
    }

    // методы для AVL
    private void inorderAssignXAVL(AVLTree.AVLNodeWrapper node,
                                   Map<AVLTree.AVLNodeWrapper, Integer> xMap,
                                   int[] xCounter) { // аналогично КЧД - тот же алгоритм inorder-обхода для AVL оберток
        if (node == null) return;
        inorderAssignXAVL(node.left, xMap, xCounter);
        xMap.put(node, xCounter[0]++);
        inorderAssignXAVL(node.right, xMap, xCounter);
    }

    private void drawEdgesAVL(GraphicsContext g,
                              AVLTree.AVLNodeWrapper node,
                              Map<AVLTree.AVLNodeWrapper, Integer> xMap,
                              double colWidth) { // рисуем связи
        if (node == null) return;
        double x = (xMap.get(node)+0.5) * colWidth;
        double y = 30 + node.depth * LEVEL_HEIGHT;

        if (node.left != null) {
            double lx = (xMap.get(node.left)+0.5) * colWidth;
            double ly = 30 + node.left.depth * LEVEL_HEIGHT;
            if (isPointInBounds(x, y) && isPointInBounds(lx, ly)) {
                g.setStroke(Color.GRAY);
                g.strokeLine(x, y, lx, ly);
            }
            drawEdgesAVL(g, node.left, xMap, colWidth);
        }
        if (node.right != null) {
            double rx = (xMap.get(node.right)+0.5) * colWidth;
            double ry = 30 + node.right.depth * LEVEL_HEIGHT;
            if (isPointInBounds(x, y) && isPointInBounds(rx, ry)) {
                g.setStroke(Color.GRAY);
                g.strokeLine(x, y, rx, ry);
            }
            drawEdgesAVL(g, node.right, xMap, colWidth);
        }
    }

    private void drawNodesAVL(GraphicsContext g,
                              AVLTree.AVLNodeWrapper node,
                              Map<AVLTree.AVLNodeWrapper,Integer> xMap,
                              double colWidth) { // рисум узлы
        if (node == null) return;
        drawNodesAVL(g, node.left, xMap, colWidth);

        double x = (xMap.get(node)+0.5) * colWidth;
        double y = 30 + node.depth * LEVEL_HEIGHT;

        if (!isPointInBounds(x, y)) {
            drawNodesAVL(g, node.right, xMap, colWidth);
            return;
        }

        // цвет узла зависит от баланс-фактора
        Color nodeColor = Color.DARKBLUE; // по дефолту темно-синий для balanceFactor = 0
        if (Math.abs(node.balanceFactor) > 1) {
            nodeColor = Color.DARKRED;  // красный в случае дисбаланса
        } else if (Math.abs(node.balanceFactor) == 1) {
            nodeColor = Color.DARKGREEN;  //  баланс +-1
        }

        g.setFill(nodeColor);
        g.fillOval(x - NODE_RADIUS, y - NODE_RADIUS, NODE_RADIUS*2, NODE_RADIUS*2);

        // основной ключ
        g.setFill(Color.WHITE);
        g.setTextAlign(TextAlignment.CENTER);
        g.setTextBaseline(VPos.CENTER);
        g.fillText(String.valueOf(node.key), x, y - 5);

        // balance factor
        Color bfColor = node.balanceFactor == 0 ? Color.LIGHTGREEN : // разный в зависимости от значения
                Math.abs(node.balanceFactor) == 1 ? Color.YELLOW : Color.ORANGERED;
        g.setFill(bfColor);
        g.setFont(new javafx.scene.text.Font(10));
        g.fillText("bf=" + node.balanceFactor, x, y + 10);

        drawNodesAVL(g, node.right, xMap, colWidth);
    }

    private boolean isPointInBounds(double x, double y) { // метод для проверки границ
        return x >= NODE_RADIUS && x <= getWidth() - NODE_RADIUS &&
                y >= NODE_RADIUS && y <= getHeight() - NODE_RADIUS;
    }
}