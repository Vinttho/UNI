package Aflevering_3.GOL_FOR_ASSIGNMENT;

import java.util.function.BiConsumer;

import Aflevering_3.GameOfLife_File.StdDraw;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.io.FileReader;

// This is the server where all the data is stored and calculated //
class GameOfLife {
    private int[][] life;
    //private int[][] LastMatrix;
    private boolean madeCustom = false;
    
    private int[][] directions = {
        {1, 0}, {-1, 0}, {0, 1}, {0, -1},  // Horizontal and vertical neighbors
        {-1, 1}, {1, 1}, {-1, -1}, {1, -1} // Diagonal neighbors
    };
    
    public int size;

    public int[][] getDirections() {
        return directions;
    }

    public void changeStatus(int x, int y, int status) {
        life[x][y] = status;
    }

    private int checkFile(String loadCustom, Scanner scanner)  {
        // Check if file exists
        File file = new File("C:\\Users\\vince\\OneDrive\\Skrivebord\\Codes\\VS_CODE\\UNI\\Java\\Fag\\Opgaver\\Afleveringer\\Aflevering_3\\GameOfLife_File\\GOL\\" + loadCustom);
    
        if (file.exists()) {
            try {
                // Read the file to determine the size of the grid
                Scanner lineCounter = new Scanner(new FileReader(file));
                int totalLines = 0;
    
                while (lineCounter.hasNextLine()) {
                    lineCounter.nextLine();
                    totalLines++;
                }
                lineCounter.close();
    
                life = new int[totalLines][totalLines];
    
                // Read the file again to fill in the grid
                Scanner fileScan = new Scanner(new FileReader(file));

                for (int i = totalLines-1; i >= 0; i--) {
                    for (int j = 0; j < totalLines; j++) {
                        if (fileScan.hasNextInt()) {
                            life[j][i] = fileScan.nextInt();
                        }
                    }
                }

                fileScan.close();
    
                return totalLines;
    
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("File not found");
            return 0;
        }
    
        return 0;
    }

    public void makeGrid(int size, String loadCustom, Scanner scanner) {
        if (loadCustom == null) {
            life = new int[size][size];
            this.size = size;
        } else {
            madeCustom = true;

            int fileSize = checkFile(loadCustom, scanner);

            if (fileSize == 0) {System.out.println("File not found"); return;}
            
            this.size = fileSize;
            return;
        }

        // Custom file size
        Random rand = new Random();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int alive = rand.nextInt(2);
                life[i][j] = alive;
            }
        }
    }

    public int getPoint(int x, int y) {
        if (x >= 0 && y >= 0 && x < size && y < size) {
            return life[x][y];
        } else {
            return 0;
        }
    }

    /*private void copyArray() {
        for (int i = 0; i < size; i++) {
            System.arraycopy(life[i], 0, LastMatrix[i], 0, size);
        }
    }*/

    private Set<String> historyArray = new HashSet<>(); // To track previous states

    public boolean canContinue() {
        //int points = size*size; 
        //int samePoints = 0;

        // Check if lastMatrix is null
        /*if (LastMatrix == null) {
            LastMatrix = new int[size][size];
            copyArray();
            return true;
        }*/

        if (madeCustom == true) {
            return true;
        }

        StringBuffer currentState = new StringBuffer(); // Make our array available to read easily
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                currentState.append(life[i][j]); // Turn the array into a string
            }
        }

        // Check if this state has been seen before
        if (historyArray.contains(currentState.toString())) {
            System.out.println("Repeating pattern detected!");
            return false; // Stop the simulation
        } else {
            historyArray.add(currentState.toString());
            return true;
        }

        /*for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (LastMatrix[i][j] == life[i][j]) {
                    samePoints += 1;
                }
            }
        }

        copyArray();

        if (samePoints == points) {return false;} else {return true;}*/
    }
}

// This is the client where all the visuals and high performance tasks are performed //
public class GameOfLifeMain {    
    private static int[][] changedStatus;
    private static final String DEFAULT_CUSTOM_LOAD = "pulsar.gol";
    private static final int ANIMATION_TIME_MS = 100;
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String loadCustom = DEFAULT_CUSTOM_LOAD;
        int gridSize = getGridSize(scanner);

        // Check if there is a grid size specified
        if (gridSize != 0) {
            loadCustom = null;
        }
        
        // Create the game of life simulation
        GameOfLife simulationData = new GameOfLife();
        simulationData.makeGrid(gridSize, loadCustom, scanner);
        
        scanner.close();
        startSimulation(simulationData);
    }

    // Require grid size from user if wanted.
    private static int getGridSize(Scanner scanner) {
        System.out.print("Custom Load? [Y/N]: ");
        String input = scanner.nextLine();
        
        if (input.equalsIgnoreCase("n")) {
            System.out.print("Enter size of grid: ");
            if (!scanner.hasNextInt()) {
                System.out.println("Error: input must be a positive integer.");
                scanner.close();
                System.exit(0);
            }
            return scanner.nextInt();
        }
        
        return 0; // default grid size if custom file is loaded
    }
    
    // Loading text for effects
    private static void showLoadingText(boolean start) {
        Thread loadingThread = new Thread(() -> {
            try {
                System.out.print("Loading");
                while (!Thread.currentThread().isInterrupted()) {
                    for (int i = 0; i < 3; i++) {
                        System.out.print("."); 
                        Thread.sleep(500); 
                    }
                    System.out.print("\b\b\b   \b\b\b");
                }
            } catch (InterruptedException e) {
                // Thread interrupted, stop the loading animation
            }
        });

        if (start) {
            loadingThread.start();
        } else {
            loadingThread.interrupt();
        }
    }

    // This is a general function that we utilize a lot
    private static void loopThroughGrid(GameOfLife simulationData, BiConsumer<Integer, Integer> action) {
        for (int i = 0; i < simulationData.size; i++) {
            for (int j = 0; j < simulationData.size; j++) {
                action.accept(i, j);
            }
        }
    }

    // Count the amount of neighbors that are around every cell, alive or dead
    private static int countNeighbors(GameOfLife simulationData, int row, int col) {
        int neighbors = 0;
        for (int[] dir : simulationData.getDirections()) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];
            if (simulationData.getPoint(newRow, newCol) == 1) {
                neighbors++;
            }
        }
        return neighbors;
    }

    // Start the simulation until it is over
    private static void startSimulation(GameOfLife simulationData) {
        int size = simulationData.size;
        changedStatus = new int[size][size];
        setupGrid(size);
        
        showLoadingText(true);
        
        // We will continue until nothing new happens
        while (simulationData.canContinue()) {
            System.out.println("----- NEW CYCLE -----");
            resetChangedStatus(simulationData);
            StdDraw.clear();
            
            updateGridDisplay(simulationData);
            showLoadingText(false);
            
            StdDraw.show(ANIMATION_TIME_MS);
            updateGridStatus(simulationData); // Here we initialize the next state
        }
    }

    // Setting up grid
    private static void setupGrid(int gridSize) {
        StdDraw.setXscale(-2, gridSize + 2);
        StdDraw.setYscale(-2, gridSize + 2);
        StdDraw.setPenRadius(2.0 / (5 * (gridSize + 2)));
    }
    
    // Reset the changedStatus array for next cycle
    private static void resetChangedStatus(GameOfLife simulationData) {
        loopThroughGrid(simulationData, (i, j) -> changedStatus[i][j] = ' ');
    }
    
    // Update the dots visualy if they are alive or dead
    private static void updateGridDisplay(GameOfLife simulationData) {
        loopThroughGrid(simulationData, (i, j) -> {
            if (simulationData.getPoint(i, j) == 1) {
                StdDraw.setPenColor(0, 0, 0);
                StdDraw.point(i, j);
            }
        });
    }

    // Update the grid data with the next cycle. Calculating neighbors etc...
    private static void updateGridStatus(GameOfLife simulationData) {
        loopThroughGrid(simulationData, (i, j) -> {
            int neighbors = countNeighbors(simulationData, i, j);

            // We don't update the state yet as it would then not be the next state
            if (simulationData.getPoint(i, j) == 1) {
                changedStatus[i][j] = (neighbors < 2 || neighbors > 3) ? 0 : 1;
            } else if (neighbors == 3) {
                changedStatus[i][j] = 1;
            }
        });
        
        // Changed status is now the new status
        loopThroughGrid(simulationData, (i, j) -> simulationData.changeStatus(i, j, changedStatus[i][j]));
    }
}
