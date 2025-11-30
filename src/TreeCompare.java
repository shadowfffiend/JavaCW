import java.util.ArrayList;
import java.util.Random;

public class TreeCompare {

    public static class ComparisonResult { // хранение результатов сравнения
        public final double rbtTime;
        public final double avlTime;
        public final double rbtRotations;
        public final double avlRotations;
        public final double rbtHeight;
        public final double avlHeight;
        public final double timeRatio;

        public ComparisonResult(double rbtTime, double avlTime, // конструктор
                                double rbtRotations, double avlRotations,
                                double rbtHeight, double avlHeight) {
            this.rbtTime = rbtTime;
            this.avlTime = avlTime;
            this.rbtRotations = rbtRotations;
            this.avlRotations = avlRotations;
            this.rbtHeight = rbtHeight;
            this.avlHeight = avlHeight;
            this.timeRatio = avlTime / rbtTime;
        }
    }

    public ComparisonResult compareInsert(int N, int repetitions) { // тест вставки
        long totalRBTTimeInsert = 0, totalAVLTimeInsert = 0;
        long totalRBTRot = 0, totalAVLRot = 0;
        int rbtHeight = 0, avlHeight = 0;

        for (int r = 0; r < repetitions; r++) { // цикл повторений
            RedBlackTree rtree = new RedBlackTree(); // новые деревья
            AVLTree atree = new AVLTree();

            rtree.resetRotations();
            atree.resetRotations();

            Random rnd = new Random(System.currentTimeMillis() + r); // текущее время + повторение

            ArrayList<Integer> keys = new ArrayList<>(N); // список ключей емкости N
            for (int i = 0; i < N; i++) keys.add(rnd.nextInt(1000000));

            // вставка RBT
            long t0 = System.nanoTime();
            for (int k : keys) rtree.insert(k);
            long t1 = System.nanoTime();
            totalRBTTimeInsert += (t1 - t0);
            totalRBTRot += rtree.getRotations();
            rbtHeight += rtree.getHeight();

            // вставка AVL
            long ta0 = System.nanoTime();
            for (int k : keys) atree.insert(k);
            long ta1 = System.nanoTime();
            totalAVLTimeInsert += (ta1 - ta0);
            totalAVLRot += atree.getRotations();
            avlHeight += atree.getHeight();
        }

        return new ComparisonResult(
                totalRBTTimeInsert / repetitions / 1_000_000.0, // преобразуем в мс и усредняем
                totalAVLTimeInsert / repetitions / 1_000_000.0,
                totalRBTRot / (double) repetitions, // усредняем повороты
                totalAVLRot / (double) repetitions,
                rbtHeight / (double) repetitions, // усредняем высоту
                avlHeight / (double) repetitions
        );
    }

    public ComparisonResult compareMixedOperations(int N, int repetitions) { // смешанные операции
        long totalRBTTime = 0, totalAVLTime = 0;
        long totalRBTRot = 0, totalAVLRot = 0;
        int rbtHeight = 0, avlHeight = 0;

        for (int r = 0; r < repetitions; r++) {
            RedBlackTree rtree = new RedBlackTree();
            AVLTree atree = new AVLTree();
            Random rnd = new Random(System.currentTimeMillis() + r);

            ArrayList<Integer> initialKeys = new ArrayList<>(); // хранение начальных ключей
            for (int i = 0; i < N / 2; i++) {
                int key = rnd.nextInt(1000000);
                initialKeys.add(key);
                rtree.insert(key);
                atree.insert(key);
            }

            rtree.resetRotations(); // сбрасываем счетчики поворотов дабы считать только тестовые операции
            atree.resetRotations();

            ArrayList<Integer> operations = new ArrayList<>(); // список типов операвий
            ArrayList<Integer> operationKeys = new ArrayList<>(); // список ключей

            for (int i = 0; i < N; i++) {
                int op = rnd.nextInt(10); // генерим рандомную операцию
                if (op < 4) operations.add(0); // 40% вставка(0 1 2 3)
                else if (op < 7) operations.add(1); // 30% удаление(4 5 6)
                else operations.add(2); // 30% поиск (7 8 9)
                operationKeys.add(rnd.nextInt(1000000)); // генерим ключи
            }

            // операции RBT
            long t0 = System.nanoTime();
            for (int i = 0; i < operations.size(); i++) {
                int op = operations.get(i); // получаем операцию и значение ключа
                int key = operationKeys.get(i);
                switch (op) {
                    case 0: rtree.insert(key); break;
                    case 1: rtree.delete(key); break;
                    case 2: rtree.contains(key); break;
                }
            }
            long t1 = System.nanoTime();
            totalRBTTime += (t1 - t0);
            totalRBTRot += rtree.getRotations();
            rbtHeight += rtree.getHeight();

            // используем те же самые операции и ключи для AVL
            long ta0 = System.nanoTime();
            for (int i = 0; i < operations.size(); i++) {
                int op = operations.get(i);
                int key = operationKeys.get(i);
                switch (op) {
                    case 0: atree.insert(key); break;
                    case 1: atree.delete(key); break;
                    case 2: atree.contains(key); break;
                }
            }
            long ta1 = System.nanoTime();
            totalAVLTime += (ta1 - ta0);
            totalAVLRot += atree.getRotations();
            avlHeight += atree.getHeight();
        }

        return new ComparisonResult( // создаем и возвращаем новый обьект с результатами теста
                totalRBTTime / repetitions / 1_000_000.0,
                totalAVLTime / repetitions / 1_000_000.0,
                totalRBTRot / (double) repetitions,
                totalAVLRot / (double) repetitions,
                rbtHeight / (double) repetitions,
                avlHeight / (double) repetitions
        );
    }
}