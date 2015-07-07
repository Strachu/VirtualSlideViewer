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

package virtualslideviewer;

import java.awt.Dimension;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.sf.ehcache.CacheManager;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import virtualslideviewer.bioformats.*;
import virtualslideviewer.config.ApplicationConfiguration;
import virtualslideviewer.core.*;
import virtualslideviewer.core.persistence.VirtualSlidePersistenceService;
import virtualslideviewer.imageviewing.*;
import virtualslideviewer.ui.*;
import virtualslideviewer.ui.about.*;
import virtualslideviewer.ui.config.*;
import virtualslideviewer.ui.imagelist.ImageListPresenter;
import virtualslideviewer.ui.imagelist.view.ImageListSwingPanel;
import virtualslideviewer.ui.imageviewing.*;
import virtualslideviewer.ui.progress.*;
import virtualslideviewer.ui.utils.SwingUIThreadMarshaller;
import virtualslideviewer.undo.UndoableActionSystem;

public class Main
{
	private static ApplicationConfiguration       mAppConfig  = new ApplicationConfiguration();
	private static ExecutorService                mThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	
	private static TileCache                      mCache;
	private static VirtualSlidePersistenceService mVirtualSlidePersistenceService;
	private static ImagePresentationModel         mImageViewModel;
	
	private static ImageListPresenter             mImageListPresenter;
	private static MainWindow                     mMainWindow;
	
	public static void main(String[] args)
	{
		Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.WARN);
		
		configureImagePresentationModel();
		bindImagePresentationModelToApplicationConfiguration();
		configurePersistenceService();
		createMainWindow();
	
		mImageListPresenter.addImageShowListener((image) -> 
		{
			mImageViewModel.setImage(new BufferedVirtualSlideImage(image, mCache));	
		});

		mMainWindow.show();
	}
	
	private static void configureImagePresentationModel()
	{
		PrefetchingStrategy             prefetchingStrategy      = new NeighbourPrefetchingStrategy(512);
		TileLoadingPrioritizer          tilePrioritizer          = new DistanceToCenterTilePrioritizer();
		LoadingTilePlaceholderGenerator tilePlaceholderGenerator = new DifferentResolutionsTileGenerator();
		
		VisibleImageLoader imageLoader = new AsyncVisibleImageLoader(mThreadPool, tilePlaceholderGenerator,
		                                                             prefetchingStrategy, tilePrioritizer);
		
		mImageViewModel = new ImagePresentationModel(new Camera(), imageLoader);
	}
	
	private static void bindImagePresentationModelToApplicationConfiguration()
	{
		mImageViewModel.setZoomIncrement(mAppConfig.getZoomIncrement());
		mImageViewModel.setResolutionTransitionThreshold(mAppConfig.getResolutionTransitionThreshold());
		
		mAppConfig.addPropertyChangeListener("ZoomIncrement", e ->
		{
			mImageViewModel.setZoomIncrement((double)e.getNewValue());
		});
		
		mAppConfig.addPropertyChangeListener("ResolutionTransitionThreshold", e ->
		{
			mImageViewModel.setResolutionTransitionThreshold((double)e.getNewValue());
		});	
	}
		
	private static void configurePersistenceService()
	{
		CacheManager.getInstance().addCache("DefaultCache");
		mCache = new EhcacheTileCacheAdapter(CacheManager.getInstance().getCache("DefaultCache"));
		
		mVirtualSlidePersistenceService = new VirtualSlidePersistenceService(mCache, mThreadPool);
		
		mVirtualSlidePersistenceService.addLoader(new OmeTiffLoader(new PaddingCalculator(mThreadPool)));
		mVirtualSlidePersistenceService.addLoader(new BioformatsLoader(new PaddingCalculator(mThreadPool)));
	}
	
	private static void createMainWindow()
	{
		UndoableActionSystem undoManager = new UndoableActionSystem();

		FileMenu            fileMenu            = new FileMenu();
		ImageListSwingPanel imageSelectionPanel = new ImageListSwingPanel(new Dimension(200, 150));
		MainPanel           mainPanel           = new MainPanel(imageSelectionPanel, mImageViewModel);
		                   mMainWindow          = new MainWindow(fileMenu);
	
		ProgressViewFactory progressViewFactory = new ProgressDialogFactory(mMainWindow);
		ConfigView          configView          = new ConfigDialogFactory(mMainWindow, mAppConfig);
		AboutView           aboutView           = new AboutDialog(mMainWindow);
		
		FileMenuPresenter   fileMenuPresenter   = new FileMenuPresenter(fileMenu, progressViewFactory, mVirtualSlidePersistenceService, undoManager);
		                    mImageListPresenter = new ImageListPresenter(imageSelectionPanel, undoManager, new SwingUIThreadMarshaller());
		MainPanelPresenter  mainPanelPresenter  = new MainPanelPresenter(mainPanel, mMainWindow.getMainRegion());
		MainPresenter       mainWindowPresenter = new MainPresenter(mMainWindow, configView, aboutView, mVirtualSlidePersistenceService,
		                                                            undoManager, mAppConfig);
		
		mVirtualSlidePersistenceService.addListener(mainPanelPresenter);
		mVirtualSlidePersistenceService.addListener(mImageListPresenter);
	}
}
