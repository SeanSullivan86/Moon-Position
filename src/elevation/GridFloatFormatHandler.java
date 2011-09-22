package elevation;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;


class GridFloatFormatHandler implements ElevationGridFileFormatHandler {
	private String folderPath;
	private String fileNamePrefix;
	
	private UncompressedGrid metadata;

	public GridFloatFormatHandler(String folderPath, String fileNamePrefix) {
		this.folderPath = folderPath;
		this.fileNamePrefix = fileNamePrefix;
	}
	
	@Override
	public UncompressedGrid getMetadataFromDisk() throws IOException {
		String s = readFile(folderPath + '/' + fileNamePrefix +".hdr");
		String[] lines = s.split("\n");
		
		int nRows=0, nCols=0;
		double minLongitude=0.0, maxLongitude=0.0, minLatitude=0.0, maxLatitude=0.0;
		double cellSize=0.0;
		for (String line: lines) {
			line = line.trim();
			if (line.contains("ncols")) {
				nCols = Integer.parseInt(line.substring(5).trim());
			} else if (line.contains("nrows")) {
				nRows = Integer.parseInt(line.substring(5).trim());
			} else if (line.contains("xllcorner")) {
				minLongitude = Double.parseDouble(line.substring(9).trim());
			} else if (line.contains("yllcorner")) {
				minLatitude = Double.parseDouble(line.substring(9).trim());
			} else if (line.contains("cellsize")) {
				cellSize = Double.parseDouble(line.substring(8).trim());
			}
		}
	    maxLongitude = minLongitude + cellSize*nCols;
	    maxLatitude = minLatitude + cellSize*nRows;
	    
	    
	   
	    CoordinateBounds bounds = new CoordinateBounds(minLongitude, maxLongitude, minLatitude, maxLatitude);
	    metadata = new UncompressedGrid(null, bounds, nRows, nCols);
	    return metadata;
	}
	
	public static void main(String[] args) throws IOException {
		GridFloatFormatHandler file = new GridFloatFormatHandler(".","floatn48w123_1");
		UncompressedGrid metadata = file.getMetadataFromDisk();
		System.out.println(metadata.toString());
		UncompressedGrid data = file.getElevationDataFromDisk();
		int prevValue = 10000;
		int delta = 0;
		int newValue = 0;
		Hashtable<Integer,Integer> deltas = new Hashtable<Integer,Integer>();
		
		for (int r = 0; r < 3612; r+=2) {
			for (int c = 0; c < 3612; c++) {
				newValue = data.elevations[r][c];
				delta = newValue - prevValue;
				if (!deltas.containsKey(delta)) {
					deltas.put(delta,0);
				}
				deltas.put(delta,deltas.get(delta)+1);
				prevValue = newValue;
			}
			for (int c = 3611; c >= 0; c--) {
				newValue = data.elevations[r+1][c];
				delta = newValue - prevValue;
				if (!deltas.containsKey(delta)) {
					deltas.put(delta,0);
				}
				deltas.put(delta,deltas.get(delta)+1);
				prevValue = newValue;
			}
		}
		
		for (int k: deltas.keySet()) {
			System.out.println(k + "|" + deltas.get(k));
		}
	}
	
	@Override
	public UncompressedGrid getElevationDataFromDisk() throws IOException {
	    if (metadata == null) {
	    	getMetadataFromDisk();
	    }
	    
	    DataInputStream dataIn = new DataInputStream(new BufferedInputStream(new FileInputStream(folderPath + '/' + fileNamePrefix+".flt")));
	    short[][] heights = new short[metadata.rows][metadata.cols];
	    
	    int b0=0, b1=0, b2=0, b3=0;
	    for (int r = metadata.rows-1; r >= 0; r--) {
	      for (int c=0; c < metadata.cols; c++) {
	          b0 = (int)dataIn.readUnsignedByte();
	          b1 = (int)dataIn.readUnsignedByte();   
	          b2 = (int)dataIn.readUnsignedByte();
	          b3 = (int)dataIn.readUnsignedByte();   
	          
	          int i = (((b0 & 0xff) << 24) | ((b1 & 0xff) << 16) | ((b2 & 0xff) << 8) | (b3 & 0xff));

	          heights[c][r] = (short) Math.round(Float.intBitsToFloat(i));
	      }
	    }
	    dataIn.close();
	    
	    UncompressedGrid result = new UncompressedGrid(heights, metadata.bounds, metadata.rows, metadata.cols);
	    return result;
	}
	
	public static String readFile(String filename) {
		try {
		    byte[] buffer = new byte[(int) new File(filename).length()];
		    BufferedInputStream f = new BufferedInputStream(new FileInputStream(filename));
		    f.read(buffer);
		    return new String(buffer);
		}
		catch (Exception e) {
			return null;
		}
	}
}