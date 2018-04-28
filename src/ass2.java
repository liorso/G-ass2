import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by Lior on 23/04/2018.
 */
public class ass2 {
    private static final String HORIZONTAL = "horizontal";
    private static final String VERTICAL = "vertical";
    private static final String DONE = "done";

    public static void main(String args[]){
        //------------------initial
        if(args.length != 5){
            System.out.println("Number of args should be 5, actual number: " + args.length);
            return;
        }
        BufferedImage image = loadImage(args[0]);
        int numOutputColumns = Integer.parseInt(args[1]);
        int numOutputRows = Integer.parseInt(args[2]);
        int energyType = Integer.parseInt(args[3]);
        String outputName = args[4];



        while(true) {
            double[][] energyMap = calculateEnergyMap(image, energyType); //Step 1: calculate energy
            String direction = decideDirection(image, numOutputColumns, numOutputRows);//Step 2: decide direction
            int[] seam;
            if (direction.equals(HORIZONTAL)) {
                image = rotateCW(image);
                double[][] transposedEnergyMap = transposeMatrix(energyMap);
                seam = verticalSeam(image, transposedEnergyMap);
                image = verticalRemove(image, seam);
                image = rotateCounterCW(image);
            }
            if (direction.equals(VERTICAL)) {
                seam = verticalSeam(image, energyMap);
                image = verticalRemove(image, seam); //TODO: VerticalAdd
            }
            if (direction.equals(DONE)) break;
        }

        String formatName = outputName.contains(".") ? outputName.substring(outputName.indexOf('.') + 1) : "jpg";

        try {
            ImageIO.write(image, formatName, new File(outputName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static double[][] transposeMatrix(double[][] energyMap) {
        double[][] transposed = new double[energyMap[0].length][energyMap.length];
        for(int i = 0 ; i < energyMap.length; i++){
            for(int j = 0 ; j < energyMap[0].length ; j++){
                transposed[j][i] = energyMap[i][j];
            }
        }
        return transposed;
    }

    public static BufferedImage rotateCW(BufferedImage image){
        int newHeight = image.getWidth();
        int newWidth = image.getHeight();
        BufferedImage rotated = new BufferedImage(newWidth, newHeight, image.getType());
        for(int i = 0 ; i < newWidth; i++){
            for(int j = 0; j < newHeight ; j++){
                rotated.setRGB(i, j, image.getRGB(j, newWidth - i - 1));
            }
        }
        return rotated;
    }

    public static BufferedImage rotateCounterCW(BufferedImage image){
        int newHeight = image.getWidth();
        int newWidth = image.getHeight();
        BufferedImage rotated = new BufferedImage(newWidth, newHeight, image.getType());
        for(int i = 0 ; i < newWidth; i++){
            for(int j = 0; j < newHeight ; j++){
                rotated.setRGB(i, j, image.getRGB(newHeight - j - 1, i));
            }
        }
        return rotated;
    }

    private static BufferedImage verticalRemove(BufferedImage image, int[] seam) {
        int width = image.getWidth();
        int height = image.getHeight();
        //Not sure about the type
        BufferedImage newImage = new BufferedImage(width - 1, height, image.getType());
        for(int i = 0; i < height ; i++){
            for(int j = 0; j < width ; j++){
                if(j == seam[i] && j == width - 1) continue;
                if(j <= seam[i]){//if equal it copy the pixel but next time it is going to be overwrite by else.
                    newImage.setRGB(j, i, image.getRGB(j, i));
                }
                else {
                    newImage.setRGB(j - 1, i, image.getRGB(j, i));
                }
            }
        }
        return newImage;
    }

    private static int[] verticalSeam(BufferedImage image, double[][] energyMap) {
        /*
        int width = image.getWidth();
        int height = image.getHeight();
        double[][] energySum = new double[width][height];
        for(int i = 0 ; i < width; i++){
            energySum[i][height - 1] = energyMap[i][height - 1];
        }
        //TODO: after running this should be change to diagonal moves also (1)
        for(int i = height - 2 ; i >= 0 ; i--){
            for(int j = 0 ; j < width ; j++){
                energySum[j][i] = energySum[j][i + 1] + energyMap[j][i];
            }
        }

        double minVal = energySum[0][0];
        int index = 0;
        for(int i = 1; i < width; i++){
            if(minVal > energySum[i][0]){
                minVal = energySum[i][0];
                index = i;
            }
        }

        int[] seam = new int[height];
        seam[0] = index;
        for(int i = 1; i < height; i++){
            seam[i] = index; //TODO: after running this should be change to diagonal moves also (2)
        }
        return seam;
        */

        double min;
        int width = image.getWidth();
        int height = image.getHeight();
        double[][] energySum = new double[width][height];
        for(int i = 0 ; i < width; i++){
            energySum[i][height - 1] = energyMap[i][height - 1];
        }

        for(int i = height - 2 ; i >= 0 ; i--){
            for(int j = 0 ; j < width ; j++){
                min = energySum[j][i + 1];
                if(j > 0 && min > energySum[j - 1][i + 1]){
                    min = energySum[j -1][i + 1];
                }
                if(j < width - 1 && min > energySum[j + 1][i + 1]){
                    min = energySum[j + 1][i + 1];
                }

                energySum[j][i] = min + energySum[j][i];
            }
        }

        double minVal = energySum[0][0];
        int index = 0;
        for(int i = 1; i < width; i++){
            if(minVal > energySum[i][0]){
                minVal = energySum[i][0];
                index = i;
            }
        }

        int[] seam = new int[height];
        seam[0] = index;
        for(int i = 1; i < height; i++){
            int prevIndex = seam[i - 1];
            min = energySum[prevIndex][i];
            index = prevIndex;
            if(prevIndex > 0 && min > energySum[prevIndex - 1][i]) {
                min = energySum[prevIndex - 1][i];
                index = prevIndex - 1;
            }
            if(prevIndex < width - 1 && min > energySum[prevIndex + 1][i]) {
                index = prevIndex + 1;
            }
            seam[i] = index;
        }
        return seam;
    }

    //TODO: bonus
    private static String decideDirection(BufferedImage image, int numOutputColumns, int numOutputRows) {
        if(image.getWidth() != numOutputColumns) return VERTICAL;
        if(image.getHeight() != numOutputRows) return HORIZONTAL;
        return DONE;
    }

    private static double[][] calculateEnergyMap(BufferedImage image, int energyType) {
        int height = image.getHeight();
        int width = image.getWidth();
        double[][] energyMap = new double[width][height];

        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                energyMap[i][j] = pixelEnergy(image, i, j, energyType);

            }
        }
        return energyMap;
    }

    private static double pixelEnergy(BufferedImage image, int x, int y, int energyType) {
        int numOfNeighbors = 8;
        int height = image.getHeight();
        int width = image.getWidth();
        double energy = 0;
        int rgb = image.getRGB(x, y);
        for(int i = x - 1 ; i <= x + 1 ; i++) {
            for (int j = y - 1; j <= y + 1 ; j++) {
                if(i == x && j == y) continue;
                if(i < 0 || j < 0 || i >= width || j >= height){
                    numOfNeighbors--;
                    continue;
                }
                int neighborRGB = image.getRGB(i, j);
                energy = energy + (Math.abs(((rgb >> 16) & 0xFF)- ((neighborRGB >> 16) & 0xFF))
                        + Math.abs(((rgb >> 8) & 0xFF)- ((neighborRGB >> 8) & 0xFF)) +
                        Math.abs((rgb & 0xFF)- (neighborRGB  & 0xFF))) / 3.0;
            }
        }
        energy = energy / numOfNeighbors;
        if(energyType == 1){
            energy = energy + localEntropy(image, x, y);
        }
        return energy;
    }

    private static double localEntropy(BufferedImage image, int x, int y){
        int neighbors = 25;
        double sum = 0;
        for(int i = x-2 ; i <= x + 2; i++){
            for(int j = y - 2; j <= y + 2; j++){
                if (i < 0 || j < 0 || i >= image.getWidth() || j >= image.getHeight()) {
                    neighbors--;
                    continue;
                }
                else {
                    double p = pValue(image, i, j);
                    sum = sum + (p * Math.log(p));
                }
            }
        }
        return - (sum / neighbors);
    }

    private static double pValue(BufferedImage image, int x, int y) {
        double sum = 0;
        int neighbors = 25;
        for(int i = x - 2; i <= x + 2; i++){
            for(int j = y - 2; j <= y + 2; j++){
                if(i < 0 || j < 0 || i >= image.getWidth() || j >= image.getHeight()) {
                    neighbors--;
                    continue;
                }
                sum = sum + grayValue(image, i, j);
            }
        }
        sum = sum / neighbors;
        return (sum != 0) ? grayValue(image, x, y) / sum : grayValue(image, x, y);
    }
    private static double grayValue(BufferedImage image, int x, int y){
        int rgb = image.getRGB(x, y);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = (rgb & 0xFF);
        return (r + g + b) / 3; //Maybe wighted?
    }

    public static BufferedImage loadImage(String ref) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(ref));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }
}
