package elevation;
import java.io.IOException;


interface ElevationGridFileFormatHandler {
	public UncompressedGrid getElevationDataFromDisk() throws IOException;
	
	public UncompressedGrid getMetadataFromDisk() throws IOException;
}