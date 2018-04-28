import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Lior on 23/04/2018.
 */
public class ass2 {
    private static final String HORIZONTAL = "horizontal";
    private static final String VERTICAL = "vertical";
    private static final String HORIZONTAL_ADD = "horizontalAdd";
    private static final String VERTICAL_ADD = "verticalAdd";
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
            double[][] energyMap = calculateEnergyMap(image); //Step 1: calculate energy
            String direction = decideDirection(image, numOutputColumns, numOutputRows);//Step 2: decide direction
            int[] seam;	
            if (direction.equals(HORIZONTAL)) {
                image = rotateCW(image);
                seam = verticalSeam(image, energyMap);
                image = verticalRemove(image, seam);
                image = rotateCounterCW(image);
            }
            if (direction.equals(VERTICAL)) {
                seam = verticalSeam(image, energyMap);
                image = verticalRemove(image, seam); //TODO: VerticalAdd
            }
            if (direction.equals(HORIZONTAL_ADD)) {
                image = rotateCW(image);
                int[][] seams = findSeamsToAdd(image,energyMap, numOutputColumns-image.getWidth());
                image = verticalAdd(image, seams); 
                image = rotateCounterCW(image);
            }
            if (direction.equals(VERTICAL_ADD)) {
                int[][] seams = findSeamsToAdd(image,energyMap, numOutputColumns-image.getWidth());
                
                image = verticalAdd(image, seams); 
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
    }
    
    //deans - multi seam finder
    public static int findMinIndex (double[] energySum, int[] burnt) {
    	int index = 0;
    	for (int i=0;i<burnt.length;i++){//min candidate
    		if(burnt[i]==0){
    			index = i;
    			break;
    		}
    	}
    	
    	double min = energySum[index];
    	for (int i=1; i<energySum.length; i++) {
    		if (energySum[i]<min && burnt [i]==0) {
    			index = i;
    			min = energySum[i];
    		}
    	}
    	return index;
    }
    public static double[][] energySumForward(int height, int width, double[][]energySum){
    	for (int i =1; i<height; i++) {
        	for ( int j=0; j< width; j++) {
        		//System.out.println(j);
        		double minVal = energySum[i-1][j];
        		minVal = ((j > 0) && (energySum[i-1][j-1] <minVal) ? energySum[i-1][j-1] : minVal);
        		minVal = ((j < width-1) && (energySum[i-1][j+1] <minVal) ? energySum[i-1][j+1] : minVal);
        		
        		energySum[i][j]+=minVal;
        	}
        }
    	return energySum;
    }
    public static double[][] energySumBackward(int height, int width, double[][]energySum){
    	for (int i =height-2; i>=0; i--) {
        	for ( int j=0; j< width; j++) {
        		//System.out.println(j);
        		double minVal = energySum[i+1][j];
        		minVal = ((j > 0) && (energySum[i+1][j-1] <minVal) ? energySum[i+1][j-1] : minVal);
        		minVal = ((j < width-1) && (energySum[i+1][j+1] <minVal) ? energySum[i+1][j+1] : minVal);
        		
        		energySum[i][j]+=minVal;
        	}
        }
    	return energySum;
    }
    public static double[][] energySumStraightBackWord(int height, int width, double[][] energyMap,double[][] energySum){
    	for(int i = height - 2 ; i >= 0 ; i--){
            for(int j = 0 ; j < width ; j++){
                energySum[j][i] = energySum[j][i + 1] + energyMap[j][i];
            }
        }
    	return energySum;
    }
    public static double[][] energySumStraightForward(int height, int width, double[][] energyMap,double[][] energySum){
    	for(int i = 1 ; i < height ; i++){
            for(int j = 0 ; j < width ; j++){
                energySum[i][j] = energySum[i-1][j] + energyMap[i][j];
            }
        }
    	return energySum;
    }
    public static int[][] findSeamsToAdd(BufferedImage image, double[][] energyMap, int neededSeams){
    	
    	int width = image.getWidth();
        int height = image.getHeight();
        double[][] energySum = new double[width][height];
        //copy energy for summmation
        for(int i = 0 ; i < height; i++){
        	for (int j =0; j<width; j++ ){
        		energySum[i][j] = energyMap[i][j];
        	}
            
        }
        //compute energy sum
        //energySum = energySumForward(height,width,energySum);
        //energySum = energySumBackward(height, width, energySum);
        //energySum = energySumStraightBackWord(height, width, energyMap,energySum); 
        energySum = energySumStraightForward(height, width, energyMap,energySum);
        
        //compute all the needed seams
        int[][] seams = new int [neededSeams][height]; 
        int[][] burnt = new int[height][width];
        //backtracking seams
        int seam = 0;
       
        while (seam<neededSeams) {
        	int index = findMinIndex(energySum[height-1],burnt[height-1]);
        	
        	seams[seam][height-1]=index;
    		burnt[height-1][index]=1;
    		boolean good = true;
        	for (int i = height-2;i>0;i--) {
        		
        		double minEnrg = energySum[i][index];
        		
        		index = ((index > 0) && (energyMap[i-1][index-1] <minEnrg) ? index-1: index);
        		index = ((index < width-1) && (energyMap[i-1][index+1] <minEnrg) ? index+1: index);
        		
        		seams[seam][i]=index;
        		
        		//see if the seam is good if not re-calculate
        		if(burnt[i][index]!=1){
        			burnt[i][index]=1;
        		}else{
        			good = false;
        		}
        		
        	
        	}
        	if(good || !good){
    			seam++;
    		}
        	
        }
        
        return seams;
    	
    	
    }
    
    
    
    private static BufferedImage addSeam(BufferedImage image, int[] seam, int added) {
    	
    	int width = image.getWidth();
        int height = image.getHeight();
        //Not sure about the type
        BufferedImage newImage = new BufferedImage(width + 1, height, image.getType());
        for(int i = 0; i < height ; i++){
        	//width +1
            for(int j = 0; j < width+1 ; j++){
            	
        		
            	if(j<(seam[i]+added)){//copy value
            		
            		newImage.setRGB(j,i,image.getRGB(j, i));
            	}
            	if(j==(seam[i]+added)){//add seam
            		//avg on edges
            		if(j==0){
            			newImage.setRGB(j,i,image.getRGB(j, i));
            		}
            		else if(j==width){
            			newImage.setRGB(j,i,image.getRGB(j-1, i));
            		}else{//normal case
            			
            			int left = image.getRGB(j-1, i);
            			int right = image.getRGB(j, i);
            			newImage.setRGB(j,i,(left+right)/2);
            			newImage.setRGB(j,i,image.getRGB(j, i));
            			//fix avg value
            		}
            	}
            	if(j>(seam[i]+added)){
            		newImage.setRGB(j,i,image.getRGB(j-1, i));
            	}
                
            }
        }
        return newImage;
	}
    

		private static BufferedImage verticalAdd(BufferedImage image, int[][] seams) {
        
        //Not sure about the type
        //int addedSeams = 0;
        BufferedImage newImage = image;
        //add seams one by one
        for(int i = 0; i < seams.length ; i++){
            newImage = addSeam(newImage, seams[i],i);
        }
        
        return newImage;
    }

	//TODO: bonus
    private static String decideDirection(BufferedImage image, int numOutputColumns, int numOutputRows) {
        if(image.getWidth() > numOutputColumns) return VERTICAL;
        if(image.getHeight() > numOutputRows) return HORIZONTAL;
        if(image.getWidth() < numOutputColumns) return VERTICAL_ADD;
        if(image.getHeight() < numOutputRows) return HORIZONTAL_ADD;
        return DONE;
    }

    private static double[][] calculateEnergyMap(BufferedImage image) {
        int height = image.getHeight();
        int width = image.getWidth();
        double[][] energyMap = new double[width][height];

        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                energyMap[i][j] = pixelEnergy(image, i, j);

            }
        }
        return energyMap;
    }

    private static double pixelEnergy(BufferedImage image, int x, int y) {
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
        return energy / numOfNeighbors;
    }

    //TODO
    private static double localEntropy(BufferedImage image, int x, int y){
        double gray = 0;
        for(int i = x-4 ; i <= x + 4; i++){
            for(int j = y - 4; j <= y + 4; j++){
                if (i < 0 || j < 0 || i >= image.getWidth() || j >= image.getHeight()) {
                    //TODO: Should we normalize the num of neighbors?
                }
                else {
                    gray = gray + grayValue(image, i, j);
                }
            }
        }
        double f = grayValue(image, x, y) / gray;
        return 0;
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