package com.project.yeswelazio;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.project.yeswelaziolibrary.PostData;
import com.project.yeswelaziolibrary.PostItemAdapter;
import com.project.yeswelaziolibrary.RefreshableListView;
import com.project.yeswelaziolibrary.RefreshableInterface;

public class NewsActivity extends Activity  implements RefreshableInterface {

	private enum RSSXMLTag {
		TITLE, DATE, LINK, CONTENT, GUID, IGNORETAG;
	}

	private ArrayList<PostData> listData;
	private String urlString = "http://www.yeswelazio.it/public/index.php?format=feed&type=rss&limitstart=";
	private RefreshableListView postListView;
	private PostItemAdapter postAdapter;
	private int pagnation = 0; // start from 1
	private boolean isRefreshLoading = true;
	private boolean isLoading = false;
	private ArrayList<String> guidList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_postlist);

		// check connectivity state
		/*
		 * ConnectivityManager cm = (ConnectivityManager)
		 * getSystemService(Context.CONNECTIVITY_SERVICE); NetworkInfo ni =
		 * cm.getActiveNetworkInfo(); if (ni != null && ni.isConnected()) { new
		 * RssDataController().execute(urlString + 0); } else {
		 * 
		 * }
		 */

		guidList = new ArrayList<String>();
		listData = new ArrayList<PostData>();
		postListView = (RefreshableListView) this
				.findViewById(R.id.postListView);
		postAdapter = new PostItemAdapter(this, R.layout.postitem, listData);
		postListView.setAdapter(postAdapter);
		postListView.setOnRefresh(this);
		postListView.onRefreshStart();
		postListView.setOnItemClickListener(onItemClickListener);
	}

	private OnItemClickListener onItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			PostData data = listData.get(arg2 - 1);
			
			Bundle postInfo = new Bundle();
			postInfo.putString("content", data.postContent);
			
			Intent postviewIntent = new Intent(NewsActivity.this,
					PostViewActivity.class);
			postviewIntent.putExtras(postInfo);
			startActivity(postviewIntent);
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private class RssDataController extends
			AsyncTask<String, Integer, ArrayList<PostData>> {
		private RSSXMLTag currentTag;

		@Override
		protected ArrayList<PostData> doInBackground(String... params) {
			// TODO Auto-generated method stub
			String urlStr = params[0];
			InputStream is = null;
			ArrayList<PostData> postDataList = new ArrayList<PostData>();
			try {
				URL url = new URL(urlStr);
				HttpURLConnection connection = (HttpURLConnection) url
						.openConnection();
				connection.setReadTimeout(10 * 1000);
				connection.setConnectTimeout(10 * 1000);
				connection.setRequestMethod("GET");
				connection.setDoInput(true);
				connection.connect();
				int response = connection.getResponseCode();
				Log.d("debug", "The response is: " + response);
				is = connection.getInputStream();

				// parse xml
				XmlPullParserFactory factory = XmlPullParserFactory
						.newInstance();
				factory.setNamespaceAware(true);
				XmlPullParser xpp = factory.newPullParser();
				xpp.setInput(is, null);

				int eventType = xpp.getEventType();
				PostData pdData = null;

				while (eventType != XmlPullParser.END_DOCUMENT) {
					if (eventType == XmlPullParser.START_DOCUMENT) {

					} else if (eventType == XmlPullParser.START_TAG) {
						Log.w("warming", xpp.getName());
						if (xpp.getName().equals("item")) {
							pdData = new PostData();
							currentTag = RSSXMLTag.IGNORETAG;
						} else if (xpp.getName().equals("title")) {
							currentTag = RSSXMLTag.TITLE;
						} else if (xpp.getName().equals("link")) {
							currentTag = RSSXMLTag.LINK;
						} else if (xpp.getName().equals("pubDate")) {
							currentTag = RSSXMLTag.DATE;
						} else if (xpp.getName().equals("description")) {
							currentTag = RSSXMLTag.CONTENT;
						} else if (xpp.getName().equals("guid")) {
							currentTag = RSSXMLTag.GUID;
						}
					} else if (eventType == XmlPullParser.END_TAG) {
						if (xpp.getName().equals("item")) {
							// format the data here, otherwise format data in
							// Adapter
							
							pdData.postDate = pdData.postDate.substring(5, 25);
							Log.w("DATA", "|" + pdData.postDate + "|");
							
							pdData.postDate = pdData.postDate.replaceFirst("Jan", "01");
							pdData.postDate = pdData.postDate.replaceFirst("Feb", "02");
							pdData.postDate = pdData.postDate.replaceFirst("Mar", "03");
							pdData.postDate = pdData.postDate.replaceFirst("Apr", "04");
							pdData.postDate = pdData.postDate.replaceFirst("May", "05");
							pdData.postDate = pdData.postDate.replaceFirst("Jun", "06");
							pdData.postDate = pdData.postDate.replaceFirst("Jul", "07");
							pdData.postDate = pdData.postDate.replaceFirst("Aug", "08");
							pdData.postDate = pdData.postDate.replaceFirst("Sep", "09");
							pdData.postDate = pdData.postDate.replaceFirst("Oct", "10");
							pdData.postDate = pdData.postDate.replaceFirst("Nov", "11");
							pdData.postDate = pdData.postDate.replaceFirst("Dec", "12");
							
							pdData.postDate = pdData.postDate.replaceFirst(" ", "-");
							pdData.postDate = pdData.postDate.replaceFirst(" ", "-");									
							
							Log.w("DATA", "|" + pdData.postDate + "|");
							
							postDataList.add(pdData);
							Log.w("DATE", "Data aggiunta");
							Log.w("DATA", "|" + pdData.postDate + "|");
						} else {
							currentTag = RSSXMLTag.IGNORETAG;
						}
					} else if (eventType == XmlPullParser.TEXT) {
						String content = xpp.getText();
						content = content.trim();
						Log.d("debug", content);
						if (pdData != null) {
							switch (currentTag) {
							case TITLE:
								if (content.length() != 0) {
									if (pdData.postTitle != null) {
										pdData.postTitle += content;
									} else {
										pdData.postTitle = content;
									}
								}
								break;
							case LINK:
								if (content.length() != 0) {
									if (pdData.postLink != null) {
										pdData.postLink += content;
									} else {
										pdData.postLink = content;
									}
								}
								break;
							case DATE:
								if (content.length() != 0) {
									if (pdData.postDate != null) {
										pdData.postDate += content;
									} else {
										pdData.postDate = content;
									}
								}
								break;
							case CONTENT:
								if (content.length() != 0) {
									if (pdData.postContent != null) {
										pdData.postContent += content;
										Log.w("CONTENT IF", pdData.postContent);
									} else {
										pdData.postContent = content;
										Log.w("CONTENT ELSE", pdData.postContent);
									}
								}
								break;
							case GUID:
								if (content.length() != 0) {
									if (pdData.postGuid != null) {
										pdData.postGuid += content;
									} else {
										pdData.postGuid = content;
									}
								}
								break;
							default:
								break;
							}
						}
					}

					eventType = xpp.next();
				}
				Log.v("tst", String.valueOf((postDataList.size())));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} /*catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/

			return postDataList;
		}

		@Override
		protected void onPostExecute(ArrayList<PostData> result) {
			// TODO Auto-generated method stub
			boolean isupdated = false;
			for (int i = 0; i < result.size(); i++) {
				// check if the post is already in the list
				if (guidList.contains(result.get(i).postGuid)) {
					continue;
				} else {
					isupdated = true;
					guidList.add(result.get(i).postGuid);
				}

				if (isRefreshLoading) {
					listData.add(i, result.get(i));
				} else {
					listData.add(result.get(i));
				}
			}

			if (isupdated) {
				postAdapter.notifyDataSetChanged();
			}

			isLoading = false;

			if (isRefreshLoading) {
				postListView.onRefreshComplete();
			} else {
				postListView.onLoadingMoreComplete();
			}

			super.onPostExecute(result);
		}
	}

	@Override
	public void startFresh() {
		// TODO Auto-generated method stub
		if (!isLoading) {
			isRefreshLoading = true;
			isLoading = true;
			new RssDataController().execute(urlString);
		} else {
			postListView.onRefreshComplete();
		}
	}

	@Override
	public void startLoadMore() {
		// TODO Auto-generated method stub
		if (!isLoading) {
			isRefreshLoading = false;
			isLoading = true;
			pagnation = pagnation + 24;
			new RssDataController().execute(urlString + pagnation);
			Log.w("INFO", "New URL is: " + urlString + pagnation);
		} else {
			postListView.onLoadingMoreComplete();
		}
	}
}