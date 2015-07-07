/*
 * Copyright (C) 2015 Patryk Strach
 * 
 * This file is part of Virtual Slide Viewer.
 * 
 * Virtual Slide Viewer is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * Virtual Slide Viewer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Virtual Slide Viewer.
 * If not, see <http://www.gnu.org/licenses/>.
*/

package virtualslideviewer.bioformats;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import loci.formats.MetadataTools;
import loci.formats.meta.IMetadata;
import loci.formats.meta.MetadataConverter;
import ome.xml.meta.OMEXMLMetadataRoot;
import ome.xml.model.*;
import ome.xml.model.enums.DimensionOrder;
import ome.xml.model.enums.PixelType;
import ome.xml.model.primitives.PositiveInteger;
import virtualslideviewer.core.VirtualSlide;
import virtualslideviewer.core.VirtualSlideImage;

/**
 * A class whose responsibility is to construct valid OME-XML metadata for given image.
 */
public class MetadataConstructor
{
	public final static String LOWER_RESOLUTION_IMAGE_TAG_VALUE = "LOWER_RESOLUTION";
	
	/**
	 * Constructs OME-XML metadata for a virtual slide.
	 * 
	 * If the slide already contains OME-XML metadata, any metadata entries unused by the application are preserved in their original form.
	 */
	public static IMetadata constructMetadata(VirtualSlide slide)
	{
		IMetadata metadata = MetadataTools.createOMEXMLMetadata();
		
		IMetadata sourceMetadata = retrieveSourceMetadata(slide);
		if(sourceMetadata != null)
		{
			MetadataConverter.convertMetadata(sourceMetadata, metadata);
		}

		String lowerResolutionTagID = addLowerResolutionTag(metadata);
		
		int seriesIndex = 0;
		
		for(VirtualSlideImage image : slide.getImageList())
		{
			for(int resIndex = image.getResolutionCount() - 1; resIndex >= 0 ;--resIndex)
			{
				copyImageMetadata(sourceMetadata, image, metadata, seriesIndex);
				
				metadata.setUUID(UUID.randomUUID().toString());
				metadata.setImageName(image.getName(), seriesIndex);
				metadata.setImageID(MetadataTools.createLSID("Image", seriesIndex), seriesIndex);

				metadata.setPixelsID(MetadataTools.createLSID("Pixels", seriesIndex), seriesIndex);
				metadata.setPixelsBigEndian(false, seriesIndex);
				metadata.setPixelsInterleaved(true, seriesIndex);
				metadata.setPixelsSignificantBits(new PositiveInteger(8), seriesIndex);
				metadata.setPixelsType(PixelType.UINT8, seriesIndex);
				metadata.setPixelsDimensionOrder(DimensionOrder.XYTZC, seriesIndex);
				
				metadata.setPixelsSizeX(new PositiveInteger(image.getImageSize(resIndex).width),  seriesIndex);
				metadata.setPixelsSizeY(new PositiveInteger(image.getImageSize(resIndex).height), seriesIndex);
				metadata.setPixelsSizeC(new PositiveInteger(image.getChannelCount() * (image.isRGB() ?  3 : 1)), seriesIndex);
				metadata.setPixelsSizeZ(new PositiveInteger(image.getZPlaneCount()), seriesIndex);
				metadata.setPixelsSizeT(new PositiveInteger(image.getTimePointCount()), seriesIndex);
				
				for(int channelIndex = 0; channelIndex < image.getChannelCount() ;++channelIndex)
				{
					metadata.setChannelID(MetadataTools.createLSID("Channel", seriesIndex, channelIndex), seriesIndex, channelIndex);
					metadata.setChannelSamplesPerPixel(new PositiveInteger(image.isRGB() ? 3 : 1), seriesIndex, channelIndex);
				}
				
				if(resIndex != image.getResolutionCount() - 1)
				{
					int resLevel = image.getResolutionCount() - 1 - resIndex;
					
					metadata.setImageName(String.format("%s res level %d", image.getName(), resLevel), seriesIndex);
					metadata.setImageAnnotationRef(lowerResolutionTagID, seriesIndex, metadata.getImageAnnotationRefCount(seriesIndex) + 1);
				}
				
				seriesIndex++;
			}
		}
		
		removeEntriesForRemovedImages(metadata, seriesIndex);
	
		return metadata;
	}
	
	private static IMetadata retrieveSourceMetadata(VirtualSlide sourceSlide)
	{
		if(sourceSlide instanceof BioformatsVirtualSlide)
		{
			return ((BioformatsVirtualSlide)sourceSlide).getBioformatsMetadata();
		}
		
		return null;
	}
	
	/**
	 * @return The Tag ID.
	 */
	private static String addLowerResolutionTag(IMetadata metadata)
	{
		int lowerResolutionTagIndex = getLowerResolutionTagIndex(metadata);
		if(lowerResolutionTagIndex == -1)
		{
			lowerResolutionTagIndex = getTagAnnocationCount(metadata);
		}

		metadata.setTagAnnotationID(MetadataTools.createLSID("TagAnnotation", lowerResolutionTagIndex), lowerResolutionTagIndex);
		metadata.setTagAnnotationValue(LOWER_RESOLUTION_IMAGE_TAG_VALUE, lowerResolutionTagIndex);
		metadata.setTagAnnotationDescription("Indicates that an image referencing this tag is a lower resolution version of a previous one." ,
		                                     lowerResolutionTagIndex);
		
		return metadata.getTagAnnotationID(lowerResolutionTagIndex);
	}
	
	/**
	 * Copies metadata of an image.
	 * 
	 * Required to preserve original image metadata in the case of image reordering.
	 */
	private static void copyImageMetadata(IMetadata sourceMetadata, VirtualSlideImage source, IMetadata destinationMetadata, int destinationIndex)
	{
		if(!(source instanceof BioformatsVirtualSlideImage))
			return;
		
		OMEXMLMetadataRoot sourceMetadataRoot = (OMEXMLMetadataRoot)sourceMetadata.getRoot();
		
		Image sourceImage      = sourceMetadataRoot.getImage(((BioformatsVirtualSlideImage)source).getSeriesIndex());
		Image destinationImage = new Image();
		
		destinationImage.setAcquisitionDate(sourceImage.getAcquisitionDate());
		destinationImage.setDescription(sourceImage.getDescription());
		destinationImage.setID(sourceImage.getID());
		destinationImage.setName(sourceImage.getName());
		
		if(sourceImage.getImagingEnvironment() != null)
		{
			destinationImage.setImagingEnvironment(new ImagingEnvironment(sourceImage.getImagingEnvironment()));
		}
		
		if(sourceImage.getObjectiveSettings() != null)
		{
			destinationImage.setObjectiveSettings(new ObjectiveSettings(sourceImage.getObjectiveSettings()));
		}
		
		if(sourceImage.getPixels() != null)
		{
			destinationImage.setPixels(new Pixels(sourceImage.getPixels()));
		}
		
		if(sourceImage.getStageLabel() != null)
		{
			destinationImage.setStageLabel(new StageLabel(sourceImage.getStageLabel()));
		}
		
		destinationImage.linkExperiment(sourceImage.getLinkedExperiment());
		destinationImage.linkExperimenter(sourceImage.getLinkedExperimenter());
		destinationImage.linkExperimenterGroup(sourceImage.getLinkedExperimenterGroup());
		destinationImage.linkInstrument(sourceImage.getLinkedInstrument());
		
		for(Annotation linkedAnnotation : sourceImage.copyLinkedAnnotationList())
		{
			destinationImage.linkAnnotation(linkedAnnotation);
		}
		
		for(Dataset linkedDataset : sourceImage.copyLinkedDatasetList())
		{
			destinationImage.linkDataset(linkedDataset);
		}
		
		for(MicrobeamManipulation linkedMicrobeamManipulation : sourceImage.copyLinkedMicrobeamManipulationList())
		{
			destinationImage.linkMicrobeamManipulation(linkedMicrobeamManipulation);
		}
		
		for(ROI linkedROI : sourceImage.copyLinkedROIList())
		{
			destinationImage.linkROI(linkedROI);
		}
		
		for(WellSample linkedWellSample : sourceImage.copyLinkedWellSampleList())
		{
			destinationImage.linkWellSample(linkedWellSample);
		}
		
		OMEXMLMetadataRoot destinationMetadataRoot = (OMEXMLMetadataRoot)destinationMetadata.getRoot();
		
		while(destinationIndex >= destinationMetadataRoot.sizeOfImageList())
		{
			destinationMetadataRoot.addImage(destinationImage);
		}
		
		destinationMetadataRoot.setImage(destinationIndex, destinationImage);
	}

	private static void removeEntriesForRemovedImages(IMetadata metadata, int imageCount)
	{
		OMEXMLMetadataRoot metadataRoot = (OMEXMLMetadataRoot)metadata.getRoot();
		
		List<Image> imagesToRemove = new ArrayList<>();

		for(int i = imageCount; i < metadata.getImageCount() ;++i)
		{
			imagesToRemove.add(metadataRoot.getImage(i));
		}
		
		for(Image imageToRemove : imagesToRemove)
		{
			metadataRoot.removeImage(imageToRemove);
		}
	}
	
	/**
	 * Gets the index of tag representing that given image is a lower resolution.
	 * 
	 * @return The index of tag or -1 if there is no lower resolution tag in the metadata.
	 */
	public static int getLowerResolutionTagIndex(IMetadata metadata)
	{
		for(int tagAnnotationIndex = 0; tagAnnotationIndex < getTagAnnocationCount(metadata) ;++tagAnnotationIndex)
		{
			if(metadata.getTagAnnotationValue(tagAnnotationIndex).equals(LOWER_RESOLUTION_IMAGE_TAG_VALUE))
			{
				return tagAnnotationIndex;
			}
		}	
		
		return -1;
	}
	
	private static int getTagAnnocationCount(IMetadata metadata)
	{
		try
		{
			return metadata.getTagAnnotationCount();
		}
		catch(NullPointerException e)
		{
			// getTagAnnotationCount() throws NullPointerException() when there are no tags...
			return 0;
		}
	}
}
