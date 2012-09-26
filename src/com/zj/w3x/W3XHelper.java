package com.zj.w3x;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class W3XHelper {
	
	private static Logger logger = LoggerFactory.getLogger(W3XHelper.class);
	
	public static List<Image> sumImages(List<Movie> beans) {
		List<Image> totalImgs = new ArrayList<Image>();
		for (Movie bean : beans) {
			totalImgs.addAll(bean.getImages());
		}
		return totalImgs;
	}
	
	public static List<Link> sumLinks(List<Movie> mvs) {
		List<Link> totalLinks = new ArrayList<Link>();
		for (Movie mv : mvs) {
			totalLinks.addAll(mv.getDownloadLinks());
		}
		return totalLinks;
	}
	
	public static void getTorrents(List<Movie> movies, int readTimeout, int connTimeout) {
		
		List<Link> totalTorrents = sumLinks(movies);
		logger.debug("Start get torrent, total: " + totalTorrents.size());
		ExecutorService thPool = getExecutor();
		List<Future<GetTorrentTask>> tasks = new ArrayList<Future<GetTorrentTask>>();
		
		for (Link torrent : totalTorrents) 
			tasks.add(thPool.submit(new GetTorrentTask(torrent, readTimeout, connTimeout)));
		
		int nSuccess = 0;
		for (Future<GetTorrentTask> f : tasks) {
			try {
				GetTorrentTask tsk = f.get();
				if (tsk.getTorrent().isDownload()) nSuccess++;
			}
			catch (Exception ex) {
				logger.debug(ex.getMessage());
			}
		}
		
		logger.info(String.format("Done. %1$s success, %2$s failed.", nSuccess, totalTorrents.size() - nSuccess));
		
		@SuppressWarnings("unchecked")
		Collection<Image> fails = CollectionUtils.select(totalTorrents, new Predicate() {
			
			@Override
			public boolean evaluate(Object arg0) {
				return !((Link) arg0).isDownload();
			}
		});
		
		if (fails.size() > 0) {
			logger.info("failed torrents are: ");
			String prefix = "    ";
			for (Link torrent : fails) {
				logger.info(prefix + torrent.getNeturl());
			}
		}
		
	}
	
	public static void getImages(List<Movie> beans, int readTimeout, int connTimeout) {
		List<Image> totalImgs = sumImages(beans);
		logger.info("Start download images, total: " + totalImgs.size());
		ExecutorService thPool = getExecutor();
		List<Future<GetImageTask>> futures = new ArrayList<Future<GetImageTask>>();
		
		for (Image img : totalImgs) {
			futures.add(thPool.submit(new GetImageTask(img, readTimeout, connTimeout)));
		}
		
		int nSuccess = 0;
		for (Future<GetImageTask> f : futures) {
			try {
				GetImageTask tsk = f.get();
				if (tsk.getImage().isDownload()) nSuccess++;
			}
			catch (Exception ex) {
				logger.debug(ex.getMessage());
			}
		}
		
		logger.info(String.format("Done. %1$s success, %2$s failed.", nSuccess, totalImgs.size() - nSuccess));
		
		@SuppressWarnings("unchecked")
		Collection<Image> fails = CollectionUtils.select(totalImgs, new Predicate() {
			
			@Override
			public boolean evaluate(Object arg0) {
				return !((Image) arg0).isDownload();
			}
		});
		
		if (fails.size() > 0) {
			logger.info("failed images are: ");
			String prefix = "    ";
			for (Image img : fails) {
				logger.info(prefix + img.getNeturl());
			}
		}
	}
	
	private static ExecutorService thPool = null;
	public static ExecutorService getExecutor() {
		if (thPool == null) {
			thPool = Executors.newFixedThreadPool(80);
			return thPool;
		}
		else {
			return thPool;
		}
	}
	
	public static void shutdownThreadPool() {
		if (thPool != null) thPool.shutdown();
	}
}
