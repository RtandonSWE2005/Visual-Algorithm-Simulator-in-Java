import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class VisualAlgorithmSimulator extends JFrame {
    private static final int ARRAY_SIZE = 50;
    private static final int BAR_WIDTH = 12;
    private static final int PANEL_HEIGHT = 400;
    private static final int DELAY = 50; // milliseconds

    private int[] array;
    private int[] originalArray;
    private VisualizationPanel visualPanel;
    private JComboBox<String> algorithmSelector;
    private JButton startButton, resetButton, shuffleButton;
    private JLabel statusLabel, comparisonLabel, swapLabel;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private int comparisons = 0;
    private int swaps = 0;
    private int currentIndex1 = -1, currentIndex2 = -1;
    private final Color highlightColor1 = Color.RED;
    private final Color highlightColor2 = Color.BLUE;

    public VisualAlgorithmSimulator() {
        setTitle("Visual Algorithm Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        initializeArray();
        setupUI();

        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void initializeArray() {
        array = new int[ARRAY_SIZE];
        originalArray = new int[ARRAY_SIZE];
        generateNewArray();
    }

    private void generateNewArray() {
        Random rand = new Random();
        for (int i = 0; i < ARRAY_SIZE; i++) {
            array[i] = rand.nextInt(300) + 10;
        }
        // Always save the current state as the original for reset
        System.arraycopy(array, 0, originalArray, 0, array.length);
    }

    private void setupUI() {
        // Top panel with controls
        JPanel controlPanel = new JPanel(new FlowLayout());

        algorithmSelector = new JComboBox<>(new String[]{
                "Bubble Sort", "Selection Sort", "Insertion Sort",
                "Quick Sort", "Merge Sort"
        });
        controlPanel.add(new JLabel("Algorithm:"));
        controlPanel.add(algorithmSelector);

        startButton = new JButton("Start");
        resetButton = new JButton("Reset");
        shuffleButton = new JButton("Shuffle");

        controlPanel.add(startButton);
        controlPanel.add(resetButton);
        controlPanel.add(shuffleButton);

        add(controlPanel, BorderLayout.NORTH);

        // Visualization panel
        visualPanel = new VisualizationPanel();
        add(visualPanel, BorderLayout.CENTER);

        // Status panel
        JPanel statusPanel = new JPanel(new FlowLayout());
        statusLabel = new JLabel("Ready");
        comparisonLabel = new JLabel("Comparisons: 0");
        swapLabel = new JLabel("Swaps: 0");

        statusPanel.add(statusLabel);
        statusPanel.add(new JLabel(" | "));
        statusPanel.add(comparisonLabel);
        statusPanel.add(new JLabel(" | "));
        statusPanel.add(swapLabel);

        add(statusPanel, BorderLayout.SOUTH);

        // Event listeners
        setupEventListeners();
    }

    private void setupEventListeners() {
        startButton.addActionListener(e -> {
            if (!isRunning.get()) {
                startSorting();
            }
        });

        resetButton.addActionListener(e -> resetArray());
        shuffleButton.addActionListener(e -> shuffleArray());
    }

    private void startSorting() {
        if (isRunning.get()) return;

        isRunning.set(true);
        startButton.setEnabled(false);
        algorithmSelector.setEnabled(false);
        resetStats();

        String selectedAlgorithm = (String) algorithmSelector.getSelectedItem();
        statusLabel.setText("Running: " + selectedAlgorithm);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                switch (selectedAlgorithm) {
                    case "Bubble Sort":
                        bubbleSort();
                        break;
                    case "Selection Sort":
                        selectionSort();
                        break;
                    case "Insertion Sort":
                        insertionSort();
                        break;
                    case "Quick Sort":
                        quickSort(0, array.length - 1);
                        break;
                    case "Merge Sort":
                        mergeSort(0, array.length - 1);
                        break;
                }
                return null;
            }

            @Override
            protected void done() {
                isRunning.set(false);
                startButton.setEnabled(true);
                algorithmSelector.setEnabled(true);
                statusLabel.setText("Completed: " + selectedAlgorithm);
                clearHighlights();
                visualPanel.repaint();
            }
        };

        worker.execute();
    }

    private void bubbleSort() throws InterruptedException {
        int n = array.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                highlightIndices(j, j + 1);
                incrementComparisons();

                if (array[j] > array[j + 1]) {
                    swap(j, j + 1);
                    incrementSwaps();
                }

                Thread.sleep(DELAY);
            }
        }
    }

    private void selectionSort() throws InterruptedException {
        int n = array.length;
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            highlightIndices(i, minIdx);

            for (int j = i + 1; j < n; j++) {
                highlightIndices(i, j);
                incrementComparisons();
                Thread.sleep(DELAY);

                if (array[j] < array[minIdx]) {
                    minIdx = j;
                }
            }

            if (minIdx != i) {
                swap(i, minIdx);
                incrementSwaps();
                Thread.sleep(DELAY);
            }
        }
    }

    private void insertionSort() throws InterruptedException {
        for (int i = 1; i < array.length; i++) {
            int key = array[i];
            int j = i - 1;

            while (j >= 0) {
                highlightIndices(j, j + 1);
                incrementComparisons();
                Thread.sleep(DELAY);

                if (array[j] <= key) break;

                array[j + 1] = array[j];
                incrementSwaps();
                visualPanel.repaint();
                Thread.sleep(DELAY);
                j--;
            }
            array[j + 1] = key;
            visualPanel.repaint();
        }
    }

    private void quickSort(int low, int high) throws InterruptedException {
        if (low < high) {
            int pi = partition(low, high);
            quickSort(low, pi - 1);
            quickSort(pi + 1, high);
        }
    }

    private int partition(int low, int high) throws InterruptedException {
        int pivot = array[high];
        int i = low - 1;

        for (int j = low; j < high; j++) {
            highlightIndices(j, high);
            incrementComparisons();
            Thread.sleep(DELAY);

            if (array[j] < pivot) {
                i++;
                swap(i, j);
                incrementSwaps();
                Thread.sleep(DELAY);
            }
        }

        swap(i + 1, high);
        incrementSwaps();
        Thread.sleep(DELAY);

        return i + 1;
    }

    private void mergeSort(int left, int right) throws InterruptedException {
        if (left < right) {
            int mid = left + (right - left) / 2;
            mergeSort(left, mid);
            mergeSort(mid + 1, right);
            merge(left, mid, right);
        }
    }

    private void merge(int left, int mid, int right) throws InterruptedException {
        int[] leftArr = new int[mid - left + 1];
        int[] rightArr = new int[right - mid];

        System.arraycopy(array, left, leftArr, 0, leftArr.length);
        System.arraycopy(array, mid + 1, rightArr, 0, rightArr.length);

        int i = 0, j = 0, k = left;

        while (i < leftArr.length && j < rightArr.length) {
            highlightIndices(k, k);
            incrementComparisons();
            Thread.sleep(DELAY);

            if (leftArr[i] <= rightArr[j]) {
                array[k] = leftArr[i];
                i++;
            } else {
                array[k] = rightArr[j];
                j++;
            }
            incrementSwaps();
            k++;
            visualPanel.repaint();
        }

        while (i < leftArr.length) {
            array[k] = leftArr[i];
            highlightIndices(k, k);
            i++;
            k++;
            incrementSwaps();
            visualPanel.repaint();
            Thread.sleep(DELAY);
        }

        while (j < rightArr.length) {
            array[k] = rightArr[j];
            highlightIndices(k, k);
            j++;
            k++;
            incrementSwaps();
            visualPanel.repaint();
            Thread.sleep(DELAY);
        }
    }

    private void swap(int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
        visualPanel.repaint();
    }

    private void highlightIndices(int i, int j) {
        currentIndex1 = i;
        currentIndex2 = j;
        SwingUtilities.invokeLater(() -> visualPanel.repaint());
    }

    private void clearHighlights() {
        currentIndex1 = -1;
        currentIndex2 = -1;
    }

    private void incrementComparisons() {
        comparisons++;
        SwingUtilities.invokeLater(() -> comparisonLabel.setText("Comparisons: " + comparisons));
    }

    private void incrementSwaps() {
        swaps++;
        SwingUtilities.invokeLater(() -> swapLabel.setText("Swaps: " + swaps));
    }

    private void resetStats() {
        comparisons = 0;
        swaps = 0;
        comparisonLabel.setText("Comparisons: 0");
        swapLabel.setText("Swaps: 0");
    }

    private void resetArray() {
        if (isRunning.get()) return;

        // Copy the original unsorted array back
        System.arraycopy(originalArray, 0, array, 0, array.length);
        clearHighlights();
        resetStats();
        statusLabel.setText("Array Reset");
        visualPanel.repaint();
    }

    private void shuffleArray() {
        if (isRunning.get()) return;

        generateNewArray();
        clearHighlights();
        resetStats();
        statusLabel.setText("Array Shuffled");
        visualPanel.repaint();
    }

    private class VisualizationPanel extends JPanel {
        public VisualizationPanel() {
            setPreferredSize(new Dimension(ARRAY_SIZE * BAR_WIDTH + 50, PANEL_HEIGHT));
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int maxValue = getMaxValue();
            int baseY = getHeight() - 50;

            for (int i = 0; i < array.length; i++) {
                int barHeight = (int) ((double) array[i] / maxValue * (baseY - 50));
                int x = 25 + i * BAR_WIDTH;
                int y = baseY - barHeight;

                // Determine bar color
                Color barColor = Color.LIGHT_GRAY;
                if (i == currentIndex1) {
                    barColor = highlightColor1;
                } else if (i == currentIndex2) {
                    barColor = highlightColor2;
                }

                g2d.setColor(barColor);
                g2d.fillRect(x, y, BAR_WIDTH - 2, barHeight);

                g2d.setColor(Color.BLACK);
                g2d.drawRect(x, y, BAR_WIDTH - 2, barHeight);

                // Draw value on top of bar
                g2d.setFont(new Font("Arial", Font.PLAIN, 8));
                String value = String.valueOf(array[i]);
                FontMetrics fm = g2d.getFontMetrics();
                int textX = x + (BAR_WIDTH - fm.stringWidth(value)) / 2;
                g2d.drawString(value, textX, y - 2);
            }

            // Draw legend
            g2d.setColor(highlightColor1);
            g2d.fillRect(10, 10, 15, 15);
            g2d.setColor(Color.BLACK);
            g2d.drawString("Comparison Index 1", 30, 22);

            g2d.setColor(highlightColor2);
            g2d.fillRect(10, 30, 15, 15);
            g2d.setColor(Color.BLACK);
            g2d.drawString("Comparison Index 2", 30, 42);
        }

        private int getMaxValue() {
            int max = array[0];
            for (int value : array) {
                if (value > max) max = value;
            }
            return max;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            new VisualAlgorithmSimulator().setVisible(true);
        });
    }
}