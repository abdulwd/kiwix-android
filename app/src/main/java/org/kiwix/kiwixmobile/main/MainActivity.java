/*
 * Copyright 2013 Rashiq Ahmad <rashiq.z@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU  General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */

package org.kiwix.kiwixmobile.main;

import android.Manifest;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.kiwix.kiwixmobile.BuildConfig;
import org.kiwix.kiwixmobile.PageBottomTab;
import org.kiwix.kiwixmobile.R;
import org.kiwix.kiwixmobile.TabDrawerAdapter;
import org.kiwix.kiwixmobile.TableDrawerAdapter;
import org.kiwix.kiwixmobile.WebViewCallback;
import org.kiwix.kiwixmobile.ZimContentProvider;
import org.kiwix.kiwixmobile.base.BaseActivity;
import org.kiwix.kiwixmobile.bookmarks_view.BookmarksActivity;
import org.kiwix.kiwixmobile.database.BookmarksDao;
import org.kiwix.kiwixmobile.search.SearchActivity;
import org.kiwix.kiwixmobile.settings.KiwixSettingsActivity;
import org.kiwix.kiwixmobile.utils.DimenUtils;
import org.kiwix.kiwixmobile.utils.DocumentParser;
import org.kiwix.kiwixmobile.utils.KiwixSearchWidget;
import org.kiwix.kiwixmobile.utils.KiwixTextToSpeech;
import org.kiwix.kiwixmobile.utils.LanguageUtils;
import org.kiwix.kiwixmobile.utils.NetworkUtils;
import org.kiwix.kiwixmobile.utils.RateAppCounter;
import org.kiwix.kiwixmobile.utils.SharedPreferenceUtil;
import org.kiwix.kiwixmobile.utils.StyleUtils;
import org.kiwix.kiwixmobile.utils.files.FileReader;
import org.kiwix.kiwixmobile.utils.files.FileUtils;
import org.kiwix.kiwixmobile.views.AnimatedProgressBar;
import org.kiwix.kiwixmobile.views.CompatFindActionModeCallback;
import org.kiwix.kiwixmobile.views.web.KiwixWebView;
import org.kiwix.kiwixmobile.views.web.ToolbarScrollingKiwixWebView;
import org.kiwix.kiwixmobile.views.web.ToolbarStaticKiwixWebView;
import org.kiwix.kiwixmobile.zim_manager.ZimManageActivity;
import org.kiwix.kiwixmobile.zim_manager.library_view.LibraryFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import de.mrapp.android.tabswitcher.AddTabButtonListener;
import de.mrapp.android.tabswitcher.Layout;
import de.mrapp.android.tabswitcher.PeekAnimation;
import de.mrapp.android.tabswitcher.PullDownGesture;
import de.mrapp.android.tabswitcher.RevealAnimation;
import de.mrapp.android.tabswitcher.SwipeGesture;
import de.mrapp.android.tabswitcher.Tab;
import de.mrapp.android.tabswitcher.TabSwitcher;
import de.mrapp.android.tabswitcher.TabSwitcherDecorator;
import de.mrapp.android.tabswitcher.TabSwitcherListener;
import de.mrapp.android.util.ThemeUtil;
import okhttp3.OkHttpClient;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES;
import static de.mrapp.android.util.DisplayUtil.getDisplayWidth;
import static org.kiwix.kiwixmobile.TableDrawerAdapter.DocumentSection;
import static org.kiwix.kiwixmobile.TableDrawerAdapter.TableClickListener;
import static org.kiwix.kiwixmobile.search.SearchActivity.EXTRA_SEARCH_IN_TEXT;
import static org.kiwix.kiwixmobile.utils.Constants.BOOKMARK_CHOSEN_REQUEST;
import static org.kiwix.kiwixmobile.utils.Constants.CONTACT_EMAIL_ADDRESS;
import static org.kiwix.kiwixmobile.utils.Constants.EXTRA_BOOKMARK_CLICKED;
import static org.kiwix.kiwixmobile.utils.Constants.EXTRA_BOOKMARK_CONTENTS;
import static org.kiwix.kiwixmobile.utils.Constants.EXTRA_CHOSE_X_TITLE;
import static org.kiwix.kiwixmobile.utils.Constants.EXTRA_CHOSE_X_URL;
import static org.kiwix.kiwixmobile.utils.Constants.EXTRA_EXTERNAL_LINK;
import static org.kiwix.kiwixmobile.utils.Constants.EXTRA_IS_WIDGET_SEARCH;
import static org.kiwix.kiwixmobile.utils.Constants.EXTRA_IS_WIDGET_STAR;
import static org.kiwix.kiwixmobile.utils.Constants.EXTRA_IS_WIDGET_VOICE;
import static org.kiwix.kiwixmobile.utils.Constants.EXTRA_LIBRARY;
import static org.kiwix.kiwixmobile.utils.Constants.EXTRA_NOTIFICATION_ID;
import static org.kiwix.kiwixmobile.utils.Constants.EXTRA_ZIM_FILE;
import static org.kiwix.kiwixmobile.utils.Constants.EXTRA_ZIM_FILE_2;
import static org.kiwix.kiwixmobile.utils.Constants.PREF_KIWIX_MOBILE;
import static org.kiwix.kiwixmobile.utils.Constants.REQUEST_FILE_SEARCH;
import static org.kiwix.kiwixmobile.utils.Constants.REQUEST_FILE_SELECT;
import static org.kiwix.kiwixmobile.utils.Constants.REQUEST_PREFERENCES;
import static org.kiwix.kiwixmobile.utils.Constants.REQUEST_STORAGE_PERMISSION;
import static org.kiwix.kiwixmobile.utils.Constants.RESULT_HISTORY_CLEARED;
import static org.kiwix.kiwixmobile.utils.Constants.RESULT_RESTART;
import static org.kiwix.kiwixmobile.utils.Constants.TAG_CURRENT_ARTICLES;
import static org.kiwix.kiwixmobile.utils.Constants.TAG_CURRENT_FILE;
import static org.kiwix.kiwixmobile.utils.Constants.TAG_CURRENT_POSITIONS;
import static org.kiwix.kiwixmobile.utils.Constants.TAG_CURRENT_TAB;
import static org.kiwix.kiwixmobile.utils.Constants.TAG_FILE_SEARCHED;
import static org.kiwix.kiwixmobile.utils.Constants.TAG_KIWIX;
import static org.kiwix.kiwixmobile.utils.StyleUtils.dialogStyle;

public class MainActivity extends BaseActivity implements WebViewCallback, TabSwitcherListener {

  public static boolean isFullscreenOpened;
  public static boolean refresh;
  public static boolean wifiOnly;
  public static boolean nightMode;
  private static Uri KIWIX_LOCAL_MARKET_URI;
  private static Uri KIWIX_BROWSER_MARKET_URI;
  public List<DocumentSection> documentSections;
  public Menu menu;
  protected boolean requestClearHistoryAfterLoad = false;
  protected boolean requestInitAllMenuItems = false;

  Toolbar toolbar;

  Button backToTopButton;

  Button stopTTSButton;
  Button pauseTTSButton;
  LinearLayout TTSControls;
  RelativeLayout toolbarContainer;
  AnimatedProgressBar progressBar;
  ImageButton exitFullscreenButton;
  CoordinatorLayout snackbarLayout;
  RelativeLayout newTabButton;
  DrawerLayout drawerLayout;
  LinearLayout tabDrawerLeftContainer;
  LinearLayout tableDrawerRightContainer;
  RecyclerView tabDrawerLeft;
  RecyclerView tableDrawerRight;
  FrameLayout contentFrame;
  ImageView tabBackButton;
  ImageView tabForwardButton;
  View tabBackButtonContainer;
  View tabForwardButtonContainer;
  TabLayout pageBottomTabLayout;
  @Inject
  OkHttpClient okHttpClient;
  @Inject
  SharedPreferenceUtil sharedPreferenceUtil;
  @Inject
  BookmarksDao bookmarksDao;
  private boolean isBackToTopEnabled = false;
  private boolean wasHideToolbar = true;
  private boolean isHideToolbar = true;
  private boolean isSpeaking = false;
  private boolean isOpenNewTabInBackground;
  private boolean isExternalLinkPopup;
  private String documentParserJs;
  private DocumentParser documentParser;
  private MenuItem menuBookmarks;
  private ArrayList<String> bookmarks;
  private List<KiwixWebView> mWebViews = new ArrayList<>();
  private KiwixTextToSpeech tts;
  private CompatFindActionModeCallback compatCallback;
  private TabDrawerAdapter tabDrawerAdapter;
  private int currentWebViewIndex = 0;
  private File file;
  private ActionMode actionMode = null;
  private KiwixWebView tempForUndo;
  private RateAppCounter visitCounterPref;
  private int tempVisitCount;
  private boolean isFirstRun;
  private PageBottomTab.Callback pageActionTabsCallback = new PageBottomTab.Callback() {
    @Override
    public void onHomeTabSelected() {
      openMainPage();
    }

    @Override
    public void onFindInPageTabSelected() {
      compatCallback.setActive();
      compatCallback.setWebView(getCurrentWebView());
      startSupportActionMode(compatCallback);
      compatCallback.showSoftInput();
    }

    @Override
    public void onFullscreenTabSelected() {
      if (isFullscreenOpened) {
        closeFullScreen();
      } else {
        openFullScreen();
      }
    }

    @Override
    public void onRandomArticleTabSelected() {
      openRandomArticle();
    }

    @Override
    public void onBookmarkTabSelected() {
      toggleBookmark();
    }

    @Override
    public void onBookmarkTabLongClicked() {
      goToBookmarks();
    }
  };
  @NonNull
  private final TabLayout.OnTabSelectedListener pageBottomTabListener
      = new TabLayout.OnTabSelectedListener() {
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
      PageBottomTab.of(tab.getPosition()).select(pageActionTabsCallback);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
      onTabSelected(tab);
    }
  };

  public static void updateWidgets(Context context) {
    Intent intent = new Intent(context.getApplicationContext(), KiwixSearchWidget.class);
    intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
    // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
    // since it seems the onUpdate() is only fired on that:
    AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
    int[] ids = widgetManager.getAppWidgetIds(new ComponentName(context, KiwixSearchWidget.class));

    widgetManager.notifyAppWidgetViewDataChanged(ids, android.R.id.list);
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
    context.sendBroadcast(intent);
  }

  @Override
  public void onActionModeStarted(ActionMode mode) {
    if (actionMode == null) {
      actionMode = mode;
      Menu menu = mode.getMenu();
      // Inflate custom menu icon.
      getMenuInflater().inflate(R.menu.menu_webview_action, menu);
      readAloudSelection(menu);
    }
    super.onActionModeStarted(mode);
  }

  @Override
  public void onActionModeFinished(ActionMode mode) {
    actionMode = null;
    super.onActionModeFinished(mode);
  }

  private void readAloudSelection(Menu menu) {
    if (menu != null) {
      menu.findItem(R.id.menu_speak_text)
          .setOnMenuItemClickListener(item -> {
            tts.readSelection(getCurrentWebView());
            if (actionMode != null) {
              actionMode.finish();
            }
            return true;
          });
    }
  }

  /**
   * The name of the extra, which is used to store the view type of a tab within a bundle.
   */
  private static final String VIEW_TYPE_EXTRA = MainActivity.class.getName() + "::ViewType";

  private void backToTopAppearDaily() {
    backToTopButton.setAlpha(0.6f);
    backToTopButton.setBackgroundColor(getResources().getColor(R.color.back_to_top_background));
    backToTopButton.setTextColor(getResources().getColor(R.color.back_to_top_text));
  }

  private void backToTopAppearNightly() {
    backToTopButton.setAlpha(0.7f);
    backToTopButton.setBackgroundColor(getResources().getColor(R.color.back_to_top_background_night));
    backToTopButton.setTextColor(getResources().getColor(R.color.back_to_top_text_night));
  }

  private void initPlayStoreUri() {
    KIWIX_LOCAL_MARKET_URI = Uri.parse("market://details?id=" + getPackageName());
    KIWIX_BROWSER_MARKET_URI =
        Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName());
  }

  private void checkForRateDialog() {
    isFirstRun = sharedPreferenceUtil.getPrefIsFirstRun();
    visitCounterPref = new RateAppCounter(this);
    tempVisitCount = visitCounterPref.getCount();
    ++tempVisitCount;
    visitCounterPref.setCount(tempVisitCount);

    if (tempVisitCount >= 5
        && !visitCounterPref.getNoThanksState()
        && NetworkUtils.isNetworkAvailable(this) && !BuildConfig.DEBUG) {
      showRateDialog();
    }
  }

  public void showRateDialog() {
    String title = getString(R.string.rate_dialog_title);
    String message = getString(R.string.rate_dialog_msg_1) + " "
        + getString(R.string.app_name)
        + getString(R.string.rate_dialog_msg_2);
    String positive = getString(R.string.rate_dialog_positive);
    String negative = getString(R.string.rate_dialog_negative);
    String neutral = getString(R.string.rate_dialog_neutral);

    new AlertDialog.Builder(this, dialogStyle())
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(positive, (dialog, id) -> {
          visitCounterPref.setNoThanksState(true);
          goToRateApp();
        })
        .setNegativeButton(negative, (dialog, id) -> {
          visitCounterPref.setNoThanksState(true);
        })
        .setNeutralButton(neutral, (dialog, id) -> {
          tempVisitCount = 0;
          visitCounterPref.setCount(tempVisitCount);
        })
        .setIcon(ContextCompat.getDrawable(this, R.mipmap.kiwix_icon))
        .show();
  }

  private void setUpToolbar() {

    setSupportActionBar(toolbar);
  }

  private void goToSearch(boolean isVoice) {
    final String zimFile = ZimContentProvider.getZimFile();
    saveTabStates();
    Intent i = new Intent(MainActivity.this, SearchActivity.class);
    i.putExtra(EXTRA_ZIM_FILE, zimFile);
    if (isVoice) {
      i.putExtra(EXTRA_IS_WIDGET_VOICE, true);
    }
    startActivityForResult(i, REQUEST_FILE_SEARCH);
  }

  private void goToRateApp() {

    Intent goToMarket = new Intent(Intent.ACTION_VIEW, KIWIX_LOCAL_MARKET_URI);

    goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
        Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

    try {
      startActivity(goToMarket);
    } catch (ActivityNotFoundException e) {
      startActivity(new Intent(Intent.ACTION_VIEW,
          KIWIX_BROWSER_MARKET_URI));
    }
  }

  private void updateTitle(String zimFileTitle) {
    if (zimFileTitle == null || zimFileTitle.trim().isEmpty()) {
      getSupportActionBar().setTitle(createMenuText(getString(R.string.app_name)));
    } else {
      getSupportActionBar().setTitle(createMenuText(zimFileTitle));
    }
  }

  private void setUpTTS() {
    tts = new KiwixTextToSpeech(this, () -> {
      if (menu != null) {
        menu.findItem(R.id.menu_read_aloud).setVisible(true);
      }
    }, new KiwixTextToSpeech.OnSpeakingListener() {
      @Override
      public void onSpeakingStarted() {
        isSpeaking = true;
        runOnUiThread(() -> {
          menu.findItem(R.id.menu_read_aloud)
              .setTitle(createMenuItem(getResources().getString(R.string.menu_read_aloud_stop)));
          TTSControls.setVisibility(View.VISIBLE);
        });
      }

      @Override
      public void onSpeakingEnded() {
        isSpeaking = false;
        runOnUiThread(() -> {
          menu.findItem(R.id.menu_read_aloud)
              .setTitle(createMenuItem(getResources().getString(R.string.menu_read_aloud)));
          TTSControls.setVisibility(View.GONE);
          pauseTTSButton.setText(R.string.tts_pause);
        });
      }
    }, new AudioManager.OnAudioFocusChangeListener() {
      @Override
      public void onAudioFocusChange(int focusChange) {
        Log.d(TAG_KIWIX, "Focus change: " + String.valueOf(focusChange));
        if (tts.currentTTSTask == null) {
          tts.stop();
          return;
        }
        switch (focusChange) {
          case (AudioManager.AUDIOFOCUS_LOSS):
            if (!tts.currentTTSTask.paused) tts.pauseOrResume();
            pauseTTSButton.setText(R.string.tts_resume);
            break;
          case (AudioManager.AUDIOFOCUS_GAIN):
            pauseTTSButton.setText(R.string.tts_pause);
            break;
        }
      }
    });

    pauseTTSButton.setOnClickListener(view -> {
      if (tts.currentTTSTask == null) {
        tts.stop();
        return;
      }

      if (tts.currentTTSTask.paused) {
        tts.pauseOrResume();
        pauseTTSButton.setText(R.string.tts_pause);
      } else {
        tts.pauseOrResume();
        pauseTTSButton.setText(R.string.tts_resume);
      }
    });

    stopTTSButton.setOnClickListener((View view) -> tts.stop());
  }

  // Reset the Locale and change the font of all TextViews and its subclasses, if necessary
  private void handleLocaleCheck() {
    LanguageUtils.handleLocaleChange(this, sharedPreferenceUtil);
    new LanguageUtils(this).changeFont(getLayoutInflater(), sharedPreferenceUtil);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    // TODO create a base Activity class that class this.
    FileUtils.deleteCachedFiles(this);
    tts.shutdown();
  }

  private void updateTableOfContents() {
    getCurrentWebView().loadUrl("javascript:(" + documentParserJs + ")()");
  }

  private void shrinkDrawers() {
    ViewGroup.MarginLayoutParams leftLayoutMargins = (ViewGroup.MarginLayoutParams) tabDrawerLeftContainer.getLayoutParams(),
        rightLayoutMargins = (ViewGroup.MarginLayoutParams) tableDrawerRightContainer.getLayoutParams();

    leftLayoutMargins.topMargin = DimenUtils.getToolbarHeight(MainActivity.this);
    rightLayoutMargins.topMargin = DimenUtils.getToolbarHeight(MainActivity.this);
    tabDrawerLeftContainer.setLayoutParams(leftLayoutMargins);
    tableDrawerRightContainer.setLayoutParams(rightLayoutMargins);
  }

  private void expandDrawers() {
    ViewGroup.MarginLayoutParams leftLayoutMargins = (ViewGroup.MarginLayoutParams) tabDrawerLeftContainer.getLayoutParams(),
        rightLayoutMargins = (ViewGroup.MarginLayoutParams) tableDrawerRightContainer.getLayoutParams();
    leftLayoutMargins.topMargin = 0;
    rightLayoutMargins.topMargin = 0;
    tabDrawerLeftContainer.setLayoutParams(leftLayoutMargins);
    tableDrawerRightContainer.setLayoutParams(rightLayoutMargins);
  }

  private KiwixWebView getWebView(String url) {
    AttributeSet attrs = StyleUtils.getAttributes(this, R.xml.webview);
    KiwixWebView webView;
    if (!isHideToolbar) {
      webView = new ToolbarScrollingKiwixWebView(MainActivity.this, this, toolbarContainer, pageBottomTabLayout, attrs);
      ((ToolbarScrollingKiwixWebView) webView).setOnToolbarVisibilityChangeListener(
          new ToolbarScrollingKiwixWebView.OnToolbarVisibilityChangeListener() {
            @Override
            public void onToolbarDisplayed() {
              shrinkDrawers();
            }

            @Override
            public void onToolbarHidden() {
              expandDrawers();
            }
          }
      );
    } else {
      webView = new ToolbarStaticKiwixWebView(MainActivity.this, this, toolbarContainer, attrs);
    }
    webView.loadUrl(url);
    webView.loadPrefs();

    return webView;
  }

  private KiwixWebView newTab() {
    String mainPage =
        Uri.parse(ZimContentProvider.CONTENT_URI + ZimContentProvider.getMainPage()).toString();
    return newTab(mainPage);
  }

  private KiwixWebView newTab(String url) {
    KiwixWebView webView = getWebView(url);
    mWebViews.add(webView);
    selectTab(mWebViews.size() - 1);
    tabDrawerAdapter.notifyDataSetChanged();
    setUpWebView();
    documentParser.initInterface(webView);
    return webView;
  }

  private void newTabInBackground(String url) {
    KiwixWebView webView = getWebView(url);
    mWebViews.add(webView);
    tabDrawerAdapter.notifyDataSetChanged();
    setUpWebView();
    documentParser.initInterface(webView);
  }

  private void restoreTab(int index) {
    mWebViews.add(index, tempForUndo);
    tabDrawerAdapter.notifyDataSetChanged();
    selectTab(index);
    setUpWebView();
  }

  private void closeTab(int index) {
    tempForUndo = mWebViews.get(index);
    int selectedPosition = tabDrawerAdapter.getSelectedPosition();
    int newSelectedPosition = selectedPosition;

    if (index <= selectedPosition) newSelectedPosition = selectedPosition - 1;
    if (index == 0) newSelectedPosition = 0;
    if (mWebViews.size() == 1) newTab();

    mWebViews.remove(index);
    showRestoreTabSnackbar(index);
    selectTab(newSelectedPosition);
    tabDrawerAdapter.notifyDataSetChanged();
  }

  private void showRestoreTabSnackbar(final int index) {
    Snackbar snackbar = Snackbar.make(snackbarLayout,
        getString(R.string.tab_closed),
        Snackbar.LENGTH_LONG)
        .setAction(getString(R.string.undo), v -> {
          restoreTab(index);
          drawerLayout.openDrawer(GravityCompat.START);
        });
    snackbar.setActionTextColor(Color.WHITE);
    snackbar.show();
  }

  private void selectTab(int position) {
    currentWebViewIndex = position;
    tabDrawerAdapter.setSelected(position);
    contentFrame.removeAllViews();

    KiwixWebView webView = mWebViews.get(position);
    contentFrame.addView(webView);
    tabDrawerAdapter.setSelected(currentWebViewIndex);

    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
      new Handler().postDelayed(() -> drawerLayout.closeDrawers(), 150);
    }
    loadPrefs();
    if (menu != null) {
      refreshBookmarkSymbol(menu);
    }
    updateTableOfContents();

    if (!isHideToolbar) {
      ((ToolbarScrollingKiwixWebView) webView).ensureToolbarDisplayed();
    }
  }

  public KiwixWebView getCurrentWebView() {
    if (mWebViews.size() == 0) return newTab();
    if (currentWebViewIndex < mWebViews.size()) {
      return mWebViews.get(currentWebViewIndex);
    } else {
      return mWebViews.get(0);
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    KiwixWebView webView = getCurrentWebView();
    switch (item.getItemId()) {

      case R.id.menu_home:
      case android.R.id.home:
        openMainPage();
        break;

      case R.id.menu_searchintext:
        compatCallback.setActive();
        compatCallback.setWebView(webView);
        startSupportActionMode(compatCallback);
        compatCallback.showSoftInput();
        break;

      case R.id.menu_bookmarks:
        toggleBookmark();
        break;

      case R.id.menu_bookmarks_list:
        goToBookmarks();
        break;

      case R.id.menu_randomarticle:
        openRandomArticle();
        break;

      case R.id.menu_help:
        showHelpPage();
        break;

      case R.id.menu_openfile:
        manageZimFiles(0);
        break;

      case R.id.menu_settings:
        selectSettings();
        break;

      case R.id.menu_read_aloud:
        if (TTSControls.getVisibility() == View.GONE) {
          if (isBackToTopEnabled) {
            backToTopButton.setVisibility(View.INVISIBLE);
          }
        } else if (TTSControls.getVisibility() == View.VISIBLE) {
          if (isBackToTopEnabled) {
            backToTopButton.setVisibility(View.VISIBLE);
          }
        }
        readAloud();
        break;

      case R.id.menu_fullscreen:
        if (isFullscreenOpened) {
          closeFullScreen();
        } else {
          openFullScreen();
        }
        break;

      default:
        break;
    }

    return super.onOptionsItemSelected(item);
  }

  private void goToBookmarks() {
    saveTabStates();
    Intent intentBookmarks = new Intent(getBaseContext(), BookmarksActivity.class);
    // FIXME: Looks like EXTRA below isn't used anywhere?
    intentBookmarks.putExtra(EXTRA_BOOKMARK_CONTENTS, bookmarks);
    startActivityForResult(intentBookmarks, BOOKMARK_CHOSEN_REQUEST);
  }

  private void openFullScreen() {
    toolbarContainer.setVisibility(View.GONE);
    pageBottomTabLayout.setVisibility(View.GONE);
    if (menuBookmarks != null)
      menuBookmarks.setVisible(true);
    exitFullscreenButton.setVisibility(View.VISIBLE);
    int fullScreenFlag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
    int classicScreenFlag = WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN;
    getWindow().addFlags(fullScreenFlag);
    getWindow().clearFlags(classicScreenFlag);
    sharedPreferenceUtil.putPrefFullScreen(true);
    expandDrawers();
    isFullscreenOpened = true;
    getCurrentWebView().requestLayout();
    if (!isHideToolbar) {
      toolbarContainer.setTranslationY(0);
      this.getCurrentWebView().setTranslationY(0);
    }
  }

  private void closeFullScreen() {
    toolbarContainer.setVisibility(View.VISIBLE);
    if (sharedPreferenceUtil.getPrefBottomToolbar()) {
      pageBottomTabLayout.setVisibility(View.VISIBLE);
      menuBookmarks.setVisible(false);
    }
    exitFullscreenButton.setVisibility(View.INVISIBLE);

    int fullScreenFlag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
    int classicScreenFlag = WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN;
    getWindow().clearFlags(fullScreenFlag);
    getWindow().addFlags(classicScreenFlag);
    sharedPreferenceUtil.putPrefFullScreen(false);
    shrinkDrawers();
    isFullscreenOpened = false;
    getCurrentWebView().requestLayout();
    if (!isHideToolbar) {
      toolbarContainer.setTranslationY(DimenUtils.getTranslucentStatusBarHeight(this));
      this.getCurrentWebView().setTranslationY(DimenUtils.getToolbarAndStatusBarHeight(this));
    }
  }

  public void showHelpPage() {
    getCurrentWebView().loadUrl("file:///android_asset/help.html");
  }

  public void sendContactEmail() {
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("plain/text");
    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{CONTACT_EMAIL_ADDRESS});
    intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback in " +
        LanguageUtils.getCurrentLocale(this).getDisplayLanguage());
    startActivity(Intent.createChooser(intent, ""));
  }

  @Override
  public void openExternalUrl(Intent intent) {
    if (intent.resolveActivity(getPackageManager()) != null) {
      // Show popup with warning that this url is external and could lead to additional costs
      // or may event not work when the user is offline.
      if (intent.hasExtra(EXTRA_EXTERNAL_LINK)
          && intent.getBooleanExtra(EXTRA_EXTERNAL_LINK, false)
          && isExternalLinkPopup) {
        externalLinkPopup(intent);
      } else {
        startActivity(intent);
      }
    } else {
      String error = getString(R.string.no_reader_application_installed);
      Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }
  }

  private void externalLinkPopup(Intent intent) {
    new AlertDialog.Builder(this, dialogStyle())
        .setTitle(R.string.external_link_popup_dialog_title)
        .setMessage(R.string.external_link_popup_dialog_message)
        .setNegativeButton(android.R.string.no, (dialogInterface, i) -> {
          // do nothing
        })
        .setNeutralButton(R.string.do_not_ask_anymore, (dialogInterface, i) -> {
          sharedPreferenceUtil.putPrefExternalLinkPopup(false);
          isExternalLinkPopup = false;

          startActivity(intent);
        })
        .setPositiveButton(android.R.string.yes, (dialogInterface, i) -> startActivity(intent))
        .setIcon(android.R.drawable.ic_dialog_alert)
        .show();
  }

  public boolean openZimFile(File file, boolean clearHistory) {
    if (file.canRead() || Build.VERSION.SDK_INT < 19 || (BuildConfig.IS_CUSTOM_APP
        && Build.VERSION.SDK_INT != 23)) {
      if (file.exists()) {
        if (ZimContentProvider.setZimFile(file.getAbsolutePath()) != null) {

          if (clearHistory) {
            requestClearHistoryAfterLoad = true;
          }
          if (menu != null) {
            initAllMenuItems();
          } else {
            // Menu may not be initialized yet. In this case
            // signal to menu create to show
            requestInitAllMenuItems = true;
          }

          //Bookmarks
          bookmarks = new ArrayList<>();
          bookmarks = bookmarksDao.getBookmarks(ZimContentProvider.getId(), ZimContentProvider.getName());

          openMainPage();
          refreshBookmarks();
          return true;
        } else {
          Toast.makeText(this, getResources().getString(R.string.error_fileinvalid),
              Toast.LENGTH_LONG).show();
          showHelpPage();
        }
      } else {
        Log.w(TAG_KIWIX, "ZIM file doesn't exist at " + file.getAbsolutePath());

        Toast.makeText(this, getResources().getString(R.string.error_filenotfound), Toast.LENGTH_LONG)
            .show();
        showHelpPage();
      }
      return false;
    } else {
      this.file = file;
      ActivityCompat.requestPermissions(this,
          new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
          REQUEST_STORAGE_PERMISSION);
      if (BuildConfig.IS_CUSTOM_APP && Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
        Toast.makeText(this, getResources().getString(R.string.request_storage_custom), Toast.LENGTH_LONG)
            .show();
      } else {
        Toast.makeText(this, getResources().getString(R.string.request_storage), Toast.LENGTH_LONG)
            .show();
      }
      return false;
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode,
                                         String permissions[], int[] grantResults) {
    switch (requestCode) {
      case REQUEST_STORAGE_PERMISSION: {
        if (grantResults.length > 0
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          finish();
          Intent newZimFile = new Intent(MainActivity.this, MainActivity.class);
          newZimFile.setData(Uri.fromFile(file));
          startActivity(newZimFile);
        } else {
          AlertDialog.Builder builder = new AlertDialog.Builder(this, dialogStyle());
          builder.setMessage(getResources().getString(R.string.reboot_message));
          AlertDialog dialog = builder.create();
          dialog.show();
          finish();
        }
      }
    }
  }

  // Workaround for popup bottom menu on older devices
  private void StyleMenuButtons(Menu m) {
    // Find each menu item and set its text colour
    for (int i = 0; i < m.size(); i++) {
      m.getItem(i).setTitle(createMenuItem(m.getItem(i).getTitle().toString()));
    }
  }

  // Create a correctly colored title for menu items
  private SpannableString createMenuItem(String title) {
    SpannableString s = new SpannableString(title);
    if (nightMode) {
      s.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s.length(), 0);
    } else {
      s.setSpan(new ForegroundColorSpan(Color.BLACK), 0, s.length(), 0);
    }
    return s;
  }

  // Create a correctly colored title for menu items
  private SpannableString createMenuText(String title) {
    SpannableString s = new SpannableString(title);
    s.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s.length(), 0);
    return s;
  }

  private void initAllMenuItems() {
    try {
      menu.findItem(R.id.menu_bookmarks).setVisible(true);
      menu.findItem(R.id.menu_fullscreen).setVisible(true);
      menu.findItem(R.id.menu_home).setVisible(true);
      menu.findItem(R.id.menu_randomarticle).setVisible(true);
      menu.findItem(R.id.menu_searchintext).setVisible(true);

      MenuItem searchItem = menu.findItem(R.id.menu_search);
      searchItem.setVisible(true);
      final String zimFile = ZimContentProvider.getZimFile();
      searchItem.setOnMenuItemClickListener(item -> {
        Intent i = new Intent(MainActivity.this, SearchActivity.class);
        i.putExtra(EXTRA_ZIM_FILE, zimFile);
        startActivityForResult(i, REQUEST_FILE_SEARCH);
        overridePendingTransition(0, 0);
        return true;
      });

      toolbar.setOnClickListener(v -> {
        Intent i = new Intent(MainActivity.this, SearchActivity.class);
        i.putExtra(EXTRA_ZIM_FILE, zimFile);
        startActivityForResult(i, REQUEST_FILE_SEARCH);
        overridePendingTransition(0, 0);
      });
      toolbar.setNavigationOnClickListener(v -> {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
          drawerLayout.closeDrawer(GravityCompat.END);
        } else if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
          drawerLayout.closeDrawer(GravityCompat.START);
        } else {
          drawerLayout.openDrawer(GravityCompat.START);
        }
      });

      new Handler().post(() -> {
        ActionMenuItemView m = findViewById(R.id.menu_bookmarks);
        if (m != null) {
          findViewById(R.id.menu_bookmarks).setOnLongClickListener(view -> {
            goToBookmarks();
            return false;
          });
        }
      });

      if (tts.isInitialized()) {
        menu.findItem(R.id.menu_read_aloud).setVisible(true);
        if (isSpeaking) {
          menu.findItem(R.id.menu_read_aloud)
              .setTitle(createMenuItem(getResources().getString(R.string.menu_read_aloud_stop)));
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (event.getAction() == KeyEvent.ACTION_DOWN) {
      switch (keyCode) {
        case KeyEvent.KEYCODE_BACK:
          if (getCurrentWebView().canGoBack()) {
            getCurrentWebView().goBack();
          } else if (isFullscreenOpened) {
            closeFullScreen();
          } else if (compatCallback.mIsActive) {
            compatCallback.finish();
          } else {
            finish();
          }
          return true;
        case KeyEvent.KEYCODE_MENU:
          openOptionsMenu();
          return true;
      }
    }
    return false;
  }

  public void toggleBookmark() {
    //Check maybe need refresh
    String article = getCurrentWebView().getUrl();
    boolean isBookmark = false;
    if (article != null && !bookmarks.contains(article)) {
      saveBookmark(article, getCurrentWebView().getTitle());
      isBookmark = true;
    } else if (article != null) {
      deleteBookmark(article);
      isBookmark = false;
    }
    popBookmarkSnackbar(isBookmark);
    supportInvalidateOptionsMenu();
  }

  private void popBookmarkSnackbar(boolean isBookmark) {
    if (isBookmark) {
      Snackbar bookmarkSnackbar =
          Snackbar.make(snackbarLayout, getString(R.string.bookmark_added), Snackbar.LENGTH_LONG)
              .setAction(getString(R.string.open), v -> goToBookmarks());
      bookmarkSnackbar.setActionTextColor(getResources().getColor(R.color.white));
      bookmarkSnackbar.show();
    } else {
      Snackbar bookmarkSnackbar =
          Snackbar.make(snackbarLayout, getString(R.string.bookmark_removed), Snackbar.LENGTH_LONG);
      bookmarkSnackbar.show();
    }
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    boolean isWidgetSearch = intent.getBooleanExtra(EXTRA_IS_WIDGET_SEARCH, false);
    boolean isWidgetVoiceSearch = intent.getBooleanExtra(EXTRA_IS_WIDGET_VOICE, false);
    boolean isWidgetStar = intent.getBooleanExtra(EXTRA_IS_WIDGET_STAR, false);

    if (isWidgetStar && ZimContentProvider.getId() != null) {
      goToBookmarks();
    } else if (isWidgetSearch && ZimContentProvider.getId() != null) {
      goToSearch(false);
    } else if (isWidgetVoiceSearch && ZimContentProvider.getId() != null) {
      goToSearch(true);
    } else if (isWidgetStar || isWidgetSearch || isWidgetVoiceSearch) {
      manageZimFiles(0);
    }
  }

  private void refreshBookmarks() {
    if (bookmarks != null) {
      bookmarks.clear();
    }
    if (bookmarksDao != null) {
      bookmarks = bookmarksDao.getBookmarks(ZimContentProvider.getId(), ZimContentProvider.getName());
    }
  }

  // TODO: change saving bookbark by zim name not id
  private void saveBookmark(String articleUrl, String articleTitle) {
    bookmarksDao.saveBookmark(articleUrl, articleTitle, ZimContentProvider.getId(), ZimContentProvider.getName());
    refreshBookmarks();
  }

  private void deleteBookmark(String article) {
    bookmarksDao.deleteBookmark(article, ZimContentProvider.getId(), ZimContentProvider.getName());
    refreshBookmarks();
  }

  public boolean openArticleFromBookmarkTitle(String bookmarkTitle) {
    return openArticle(ZimContentProvider.getPageUrlFromTitle(bookmarkTitle));
  }

  private void contentsDrawerHint() {
    drawerLayout.postDelayed(() -> drawerLayout.openDrawer(GravityCompat.END), 500);

    AlertDialog.Builder builder = new AlertDialog.Builder(this, dialogStyle());
    builder.setMessage(getString(R.string.hint_contents_drawer_message))
        .setPositiveButton(getString(R.string.got_it), (dialog, id) -> {
        })
        .setTitle(getString(R.string.did_you_know))
        .setIcon(R.drawable.icon_question);
    AlertDialog alert = builder.create();
    alert.show();
  }

  private boolean openArticle(String articleUrl) {
    if (articleUrl != null) {
      getCurrentWebView().loadUrl(
          Uri.parse(ZimContentProvider.CONTENT_URI + articleUrl).toString());
    }
    return true;
  }

  public boolean openRandomArticle() {
    String articleUrl = ZimContentProvider.getRandomArticleUrl();
    Log.d(TAG_KIWIX, "openRandomArticle: " + articleUrl);
    return openArticle(articleUrl);
  }

  public boolean openMainPage() {
    String articleUrl = ZimContentProvider.getMainPage();
    return openArticle(articleUrl);
  }

  public void readAloud() {
    tts.readAloud(getCurrentWebView());
  }

  private void setUpWebView() {

    getCurrentWebView().getSettings().setJavaScriptEnabled(true);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      WebView.setWebContentsDebuggingEnabled(true);
    }

    // webView.getSettings().setLoadsImagesAutomatically(false);
    // Does not make much sense to cache data from zim files.(Not clear whether
    // this actually has any effect)
    getCurrentWebView().getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

    // Should basically resemble the behavior when setWebClient not done
    // (i.p. internal urls load in webview, external urls in browser)
    // as currently no custom setWebViewClient required it is commented
    // However, it must notify the bookmark system when a page is finished loading
    // so that it can refresh the menu.

    backToTopButton.setOnClickListener(view -> MainActivity.this.runOnUiThread(() -> getCurrentWebView().pageUp(true)));
    tts.initWebView(getCurrentWebView());
  }

  private void setUpExitFullscreenButton() {

    exitFullscreenButton.setOnClickListener(v -> closeFullScreen());
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    toggleActionItemsConfig();
  }

  void toggleActionItemsConfig() {
    if (menu != null) {
      MenuItem random = menu.findItem(R.id.menu_randomarticle);
      MenuItem home = menu.findItem(R.id.menu_home);
      if (getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE) {
        random.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        home.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
      } else {
        random.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        home.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
      }
    }
  }

  public void searchForTitle(String title) {
    String articleUrl;

    if (title.startsWith("A/")) {
      articleUrl = title;
    } else {
      articleUrl = ZimContentProvider.getPageUrlFromTitle(title);
    }
    openArticle(articleUrl);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {

    Log.i(TAG_KIWIX, "Intent data: " + data);

    switch (requestCode) {
      case REQUEST_FILE_SELECT:
        if (resultCode == RESULT_OK) {
          // The URI of the selected file
          final Uri uri = data.getData();
          File file = null;
          if (uri != null) {
            String path = uri.getPath();
            if (path != null) {
              file = new File(path);
            }
          }
          if (file == null) {
            Log.i(TAG_KIWIX, "Could not find file");
            return;
          }
          finish();
          Intent zimFile = new Intent(MainActivity.this, MainActivity.class);
          zimFile.setData(uri);
          startActivity(zimFile);
        }
        break;
      case REQUEST_FILE_SEARCH:
        if (resultCode == RESULT_OK) {
          String title =
              data.getStringExtra(TAG_FILE_SEARCHED).replace("<b>", "").replace("</b>", "");
          boolean isSearchInText = data.getBooleanExtra(EXTRA_SEARCH_IN_TEXT, false);
          if (isSearchInText) {
            //if the search is localized trigger find in page UI.
            KiwixWebView webView = getCurrentWebView();
            compatCallback.setActive();
            compatCallback.setWebView(webView);
            startSupportActionMode(compatCallback);
            compatCallback.setText(title);
            compatCallback.findAll();
            compatCallback.showSoftInput();
          } else {
            searchForTitle(title);
          }
        } else { //TODO: Inform the User
          Log.w(TAG_KIWIX, "Unhandled search failure");
        }
        break;
      case REQUEST_PREFERENCES:
        if (resultCode == RESULT_RESTART) {
          finish();
          startActivity(new Intent(MainActivity.this, MainActivity.class));
        }
        if (resultCode == RESULT_HISTORY_CLEARED) {
          mWebViews.clear();
          newTab();
          tabDrawerAdapter.notifyDataSetChanged();
        }
        loadPrefs();
        break;

      case BOOKMARK_CHOSEN_REQUEST:
        if (resultCode == RESULT_OK) {
          boolean itemClicked = data.getBooleanExtra(EXTRA_BOOKMARK_CLICKED, false);
          if (ZimContentProvider.getId() == null) return;

          //Bookmarks
          bookmarks = bookmarksDao.getBookmarks(ZimContentProvider.getId(), ZimContentProvider.getName());

          if (itemClicked) {
            String bookmarkChosen;
            if (data.getStringExtra(EXTRA_CHOSE_X_URL) != null) {
              bookmarkChosen = data.getStringExtra(EXTRA_CHOSE_X_URL);
              newTab();
              getCurrentWebView().loadUrl(bookmarkChosen);
            } else {
              newTab();
              bookmarkChosen = data.getStringExtra(EXTRA_CHOSE_X_TITLE);
              openArticleFromBookmarkTitle(bookmarkChosen);
            }
          }
          if (menu != null) {
            refreshBookmarkSymbol(menu);
          }
        }
        break;
      default:
        break;
    }

    super.onActivityResult(requestCode, resultCode, data);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu_main, menu);
    this.menu = menu;
    this.menuBookmarks = menu.findItem(R.id.menu_bookmarks);
    StyleMenuButtons(menu);
    if (BuildConfig.IS_CUSTOM_APP) {
      menu.findItem(R.id.menu_help).setVisible(false);
      menu.findItem(R.id.menu_openfile).setVisible(false);
    }

    if (requestInitAllMenuItems) {
      initAllMenuItems();
    }
    if (isFullscreenOpened) {
      openFullScreen();
    }

    if (sharedPreferenceUtil.getPrefBottomToolbar()) {
      menu.findItem(R.id.menu_bookmarks).setVisible(false);
    }

    return true;
  }

  @Override
  public boolean onMenuOpened(int featureId, Menu menu) {
    if (drawerLayout.isDrawerOpen(tabDrawerLeftContainer)) {
      drawerLayout.closeDrawer(tabDrawerLeftContainer);
    }

    if (drawerLayout.isDrawerOpen(tableDrawerRightContainer)) {
      drawerLayout.closeDrawer(tableDrawerRightContainer);
    }

    return super.onMenuOpened(featureId, menu);
  }

  // This method refreshes the menu for the bookmark system.
  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    toggleActionItemsConfig();
    refreshBookmarkSymbol(menu);
    refreshNavigationButtons();

    if (getCurrentWebView().getUrl() == null || getCurrentWebView().getUrl().equals("file:///android_asset/help.html")) {
      menu.findItem(R.id.menu_read_aloud).setVisible(false);
    } else {
      menu.findItem(R.id.menu_read_aloud).setVisible(true);
    }

    return true;
  }

  public void refreshBookmarkSymbol(Menu menu) { // Checks if current webview is in bookmarks array
    if (bookmarks == null || bookmarks.size() == 0) {
      bookmarks = bookmarksDao.getBookmarks(ZimContentProvider.getId(), ZimContentProvider.getName());
    }

    TabLayout.Tab bookmarkTab = pageBottomTabLayout.getTabAt(4);

    if (menu.findItem(R.id.menu_bookmarks) != null &&
        getCurrentWebView().getUrl() != null &&
        ZimContentProvider.getId() != null &&
        !getCurrentWebView().getUrl().equals("file:///android_asset/help.html")) {
      int icon = bookmarks.contains(getCurrentWebView().getUrl()) ? R.drawable.action_bookmark_active : R.drawable.action_bookmark;

      menu.findItem(R.id.menu_bookmarks)
          .setEnabled(true)
          .setIcon(icon)
          .getIcon().setAlpha(255);

      bookmarkTab.getCustomView().findViewById(R.id.bookmark_tab_icon).setBackgroundResource(icon);
    } else {
      menu.findItem(R.id.menu_bookmarks)
          .setEnabled(false)
          .setIcon(R.drawable.action_bookmark)
          .getIcon().setAlpha(130);

      bookmarkTab.getCustomView().findViewById(R.id.bookmark_tab_icon).setBackgroundResource(R.drawable.action_bookmark);
    }
  }

  public void refreshNavigationButtons() {
    toggleImageViewGrayFilter(tabBackButton, getCurrentWebView().canGoBack());
    toggleImageViewGrayFilter(tabForwardButton, getCurrentWebView().canGoForward());
    tabBackButtonContainer.setEnabled(getCurrentWebView().canGoBack());
    tabForwardButtonContainer.setEnabled(getCurrentWebView().canGoForward());
  }

  public void toggleImageViewGrayFilter(ImageView image, boolean state) {
    Drawable originalIcon = image.getDrawable();
    Drawable res = originalIcon.mutate();
    if (state) {
      res.clearColorFilter();
    } else {
      res.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
    }
    image.setImageDrawable(res);
  }

  public void loadPrefs() {

    nightMode = KiwixSettingsActivity.nightMode(sharedPreferenceUtil);
    isBackToTopEnabled = sharedPreferenceUtil.getPrefBackToTop();
    isHideToolbar = sharedPreferenceUtil.getPrefHideToolbar();
    isFullscreenOpened = sharedPreferenceUtil.getPrefFullScreen();
    boolean isZoomEnabled = sharedPreferenceUtil.getPrefZoomEnabled();
    isOpenNewTabInBackground = sharedPreferenceUtil.getPrefNewTabBackground();
    isExternalLinkPopup = sharedPreferenceUtil.getPrefExternalLinkPopup();

    if (isZoomEnabled) {
      int zoomScale = (int) sharedPreferenceUtil.getPrefZoom();
      getCurrentWebView().setInitialScale(zoomScale);
    } else {
      getCurrentWebView().setInitialScale(0);
    }

    if (!isBackToTopEnabled) {
      backToTopButton.setVisibility(View.INVISIBLE);
    }

    if (isFullscreenOpened) {
      openFullScreen();
    }

    // Night mode status
    if (nightMode) {
      getCurrentWebView().toggleNightMode();
    } else {
      getCurrentWebView().deactivateNightMode();
    }
  }

  public void manageZimFiles(int tab) {
    refreshBookmarks();
    final Intent target = new Intent(this, ZimManageActivity.class);
    target.setAction(Intent.ACTION_GET_CONTENT);
    // The MIME data type filter
    target.setType("//");
    target.putExtra(ZimManageActivity.TAB_EXTRA, tab);
    // Only return URIs that can be opened with ContentResolver
    target.addCategory(Intent.CATEGORY_OPENABLE);
    // Force use of our file selection component.
    // (Note may make sense to just define a custom intent instead)

    startActivityForResult(target, REQUEST_FILE_SELECT);
  }

  public void selectSettings() {
    final String zimFile = ZimContentProvider.getZimFile();
    Intent i = new Intent(this, KiwixSettingsActivity.class);
    // FIXME: I think line below is redundant - it's not used anywhere
    i.putExtra(EXTRA_ZIM_FILE_2, zimFile);
    startActivityForResult(i, REQUEST_PREFERENCES);
  }

  public void saveTabStates() {
    SharedPreferences settings = getSharedPreferences(PREF_KIWIX_MOBILE, 0);
    SharedPreferences.Editor editor = settings.edit();

    JSONArray urls = new JSONArray();
    JSONArray positions = new JSONArray();
    for (KiwixWebView view : mWebViews) {
      if (view.getUrl() == null) continue;
      urls.put(view.getUrl());
      positions.put(view.getScrollY());
    }

    editor.putString(TAG_CURRENT_FILE, ZimContentProvider.getZimFile());
    editor.putString(TAG_CURRENT_ARTICLES, urls.toString());
    editor.putString(TAG_CURRENT_POSITIONS, positions.toString());
    editor.putInt(TAG_CURRENT_TAB, currentWebViewIndex);

    editor.apply();
  }

  public void restoreTabStates() {
    SharedPreferences settings = getSharedPreferences(PREF_KIWIX_MOBILE, 0);
    String zimFile = settings.getString(TAG_CURRENT_FILE, null);
    String zimArticles = settings.getString(TAG_CURRENT_ARTICLES, null);
    String zimPositions = settings.getString(TAG_CURRENT_POSITIONS, null);

    int currentTab = settings.getInt(TAG_CURRENT_TAB, 0);

    openZimFile(new File(zimFile), false);
    try {
      JSONArray urls = new JSONArray(zimArticles);
      JSONArray positions = new JSONArray(zimPositions);
      int i = 0;
      getCurrentWebView().loadUrl(urls.getString(i));
      getCurrentWebView().setScrollY(positions.getInt(i));
      i++;
      for (; i < urls.length(); i++) {
        newTab(urls.getString(i));
        getCurrentWebView().setScrollY(positions.getInt(i));
      }
      selectTab(currentTab);
    } catch (Exception e) {
      Log.w(TAG_KIWIX, "Kiwix shared preferences corrupted", e);
      //TODO: Show to user
    }
  }

  private void manageExternalLaunchAndRestoringViewState() {

    if (getIntent().getData() != null) {
      String filePath = FileUtils.getLocalFilePathByUri(getApplicationContext(), getIntent().getData());

      if (filePath == null || !new File(filePath).exists()) {
        Toast.makeText(MainActivity.this, getString(R.string.error_filenotfound), Toast.LENGTH_LONG).show();
        return;
      }

      Log.d(TAG_KIWIX, "Kiwix started from a filemanager. Intent filePath: "
          + filePath
          + " -> open this zimfile and load menu_main page");
      openZimFile(new File(filePath), false);
    } else {
      SharedPreferences settings = getSharedPreferences(PREF_KIWIX_MOBILE, 0);
      String zimFile = settings.getString(TAG_CURRENT_FILE, null);
      if (zimFile != null && new File(zimFile).exists()) {
        Log.d(TAG_KIWIX,
            "Kiwix normal start, zimFile loaded last time -> Open last used zimFile " + zimFile);
        restoreTabStates();
        // Alternative would be to restore webView state. But more effort to implement, and actually
        // fits better normal android behavior if after closing app ("back" button) state is not maintained.
      } else {

        if (BuildConfig.IS_CUSTOM_APP) {
          Log.d(TAG_KIWIX, "Kiwix Custom App starting for the first time. Checking Companion ZIM: " + BuildConfig.ZIM_FILE_NAME);

          String currentLocaleCode = Locale.getDefault().toString();
          // Custom App recommends to start off a specific language
          if (BuildConfig.ENFORCED_LANG.length() > 0 && !BuildConfig.ENFORCED_LANG
              .equals(currentLocaleCode)) {

            // change the locale machinery
            LanguageUtils.handleLocaleChange(this, BuildConfig.ENFORCED_LANG);

            // save new locale into preferences for next startup
            sharedPreferenceUtil.putPrefLanguage(BuildConfig.ENFORCED_LANG);

            // restart activity for new locale to take effect
            this.setResult(1236);
            this.finish();
            this.startActivity(new Intent(this, this.getClass()));
          }

          String filePath = "";
          if (BuildConfig.HAS_EMBEDDED_ZIM) {
            String appPath = getPackageResourcePath();
            File libDir = new File(appPath.substring(0, appPath.lastIndexOf("/")) + "/lib/");
            if (libDir.exists() && libDir.listFiles().length > 0) {
              filePath = libDir.listFiles()[0].getPath() + "/" + BuildConfig.ZIM_FILE_NAME;
            }
            if (filePath.isEmpty() || !new File(filePath).exists()) {
              filePath = String.format("/data/data/%s/lib/%s", BuildConfig.APPLICATION_ID,
                  BuildConfig.ZIM_FILE_NAME);
            }
          } else {
            String fileName = FileUtils.getExpansionAPKFileName(true);
            filePath = FileUtils.generateSaveFileName(fileName);
          }

          if (!FileUtils.doesFileExist(filePath, BuildConfig.ZIM_FILE_SIZE, false)) {

            AlertDialog.Builder zimFileMissingBuilder = new AlertDialog.Builder(this, dialogStyle());
            zimFileMissingBuilder.setTitle(R.string.app_name);
            zimFileMissingBuilder.setMessage(R.string.customapp_missing_content);
            zimFileMissingBuilder.setIcon(R.mipmap.kiwix_icon);
            final Activity activity = this;
            zimFileMissingBuilder.setPositiveButton(getString(R.string.go_to_play_store),
                (dialog, which) -> {
                  String market_uri = "market://details?id=" + BuildConfig.APPLICATION_ID;
                  Intent intent = new Intent(Intent.ACTION_VIEW);
                  intent.setData(Uri.parse(market_uri));
                  startActivity(intent);
                  activity.finish();
                });
            zimFileMissingBuilder.setCancelable(false);
            AlertDialog zimFileMissingDialog = zimFileMissingBuilder.create();
            zimFileMissingDialog.show();
          } else {
            openZimFile(new File(filePath), true);
          }
        } else {
          Log.d(TAG_KIWIX, "Kiwix normal start, no zimFile loaded last time  -> display help page");
          showHelpPage();
        }
      }
    }
  }

  @Override
  public void onPause() {
    super.onPause();

    saveTabStates();
    refreshBookmarks();

    Log.d(TAG_KIWIX, "onPause Save currentzimfile to preferences: " + ZimContentProvider.getZimFile());
  }

  @Override
  public void webViewUrlLoading() {
    if (isFirstRun && !BuildConfig.DEBUG) {
      contentsDrawerHint();
      sharedPreferenceUtil.putPrefIsFirstRun(false);// It is no longer the first run
      isFirstRun = false;
    }
  }

  @Override
  public void webViewUrlFinishedLoading() {
    updateTableOfContents();
    tabDrawerAdapter.notifyDataSetChanged();

    if (menu != null)
      refreshBookmarkSymbol(menu);
  }

  @Override
  public void webViewFailedLoading(String url) {
    String error = String.format(getString(R.string.error_articleurlnotfound), url);
    Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
  }

  @Override
  public void webViewProgressChanged(int progress) {
    progressBar.setProgress(progress);
    if (progress == 100) {
      if (requestClearHistoryAfterLoad) {
        Log.d(TAG_KIWIX,
            "Loading article finished and requestClearHistoryAfterLoad -> clearHistory");
        getCurrentWebView().clearHistory();
        requestClearHistoryAfterLoad = false;
      }

      Log.d(TAG_KIWIX, "Loaded URL: " + getCurrentWebView().getUrl());
    }
  }

  @Override
  public void webViewTitleUpdated(String title) {
    tabDrawerAdapter.notifyDataSetChanged();
  }


  @Override
  public void webViewPageChanged(int page, int maxPages) {
    if (isBackToTopEnabled) {
      if (getCurrentWebView().getScrollY() > 200) {
        if (backToTopButton.getVisibility() == View.INVISIBLE && TTSControls.getVisibility() == View.GONE) {
          backToTopButton.setText(R.string.button_backtotop);
          backToTopButton.setVisibility(View.VISIBLE);

          backToTopButton.startAnimation(
              AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in));
          backToTopButton.setVisibility(View.INVISIBLE);
          Animation fadeAnimation =
              AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_out);
          fadeAnimation.setStartOffset(1200);
          backToTopButton.startAnimation(fadeAnimation);
        }
      } else {
        if (backToTopButton.getVisibility() == View.VISIBLE) {
          backToTopButton.setVisibility(View.INVISIBLE);

          backToTopButton.clearAnimation();
          backToTopButton.startAnimation(
              AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_out));
        } else {
          backToTopButton.clearAnimation();
        }
      }
    }
  }

  @Override
  public void webViewLongClick(final String url) {
    boolean handleEvent = false;
    if (url.startsWith(ZimContentProvider.CONTENT_URI.toString())) {
      // This is my web site, so do not override; let my WebView load the page
      handleEvent = true;
    } else if (url.startsWith("file://")) {
      // To handle help page (loaded from resources)
      handleEvent = true;
    } else if (url.startsWith(ZimContentProvider.UI_URI.toString())) {
      handleEvent = true;
    }

    if (handleEvent) {
      AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, dialogStyle());

      builder.setPositiveButton(android.R.string.yes, (dialog, id) -> {
        if (isOpenNewTabInBackground) {
          newTabInBackground(url);
          Snackbar snackbar = Snackbar.make(snackbarLayout,
              getString(R.string.new_tab_snackbar),
              Snackbar.LENGTH_LONG)
              .setAction(getString(R.string.open), v -> {
                if (mWebViews.size() > 1) selectTab(mWebViews.size() - 1);
              });
          snackbar.setActionTextColor(getResources().getColor(R.color.white));
          snackbar.show();
        } else {
          newTab(url);
        }
      });
      builder.setNegativeButton(android.R.string.no, null);
      builder.setMessage(getString(R.string.open_in_new_tab));
      AlertDialog dialog = builder.create();
      dialog.show();
    }
  }

  private TabSwitcher tabSwitcher;
  private Snackbar snackbar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

    super.onCreate(savedInstanceState);

    new WebView(this).destroy(); // Workaround for buggy webviews see #710

    wifiOnly = sharedPreferenceUtil.getPrefWifiOnly();
    nightMode = KiwixSettingsActivity.nightMode(sharedPreferenceUtil);
    if (nightMode) {
      setTheme(R.style.AppTheme_Night);
    }

    handleLocaleCheck();
    setContentView(R.layout.activity_main);

    tabSwitcher = findViewById(R.id.tab_switcher);
    tabSwitcher.clearSavedStatesWhenRemovingTabs(false);
    ViewCompat.setOnApplyWindowInsetsListener(tabSwitcher, createWindowInsetsListener());
    tabSwitcher.setDecorator(new Decorator());
    tabSwitcher.addListener(this);
    tabSwitcher.showToolbars(true);
    tabSwitcher.addTab(createTab(0));

    tabSwitcher.showAddTabButton(createAddTabButtonListener());
    tabSwitcher.setToolbarNavigationIcon(R.drawable.ic_add_box_black_32dp, createAddTabListener());
    TabSwitcher.setupWithMenu(tabSwitcher, createTabSwitcherButtonListener());
    inflateMenu();

    checkForRateDialog();

    initPlayStoreUri();

    if (SDK_INT <= VERSION_CODES.LOLLIPOP) {
      snackbarLayout.setFitsSystemWindows(true);
    }

    isHideToolbar = sharedPreferenceUtil.getPrefHideToolbar();

    FileReader fileReader = new FileReader();
    documentParserJs = fileReader.readFile("js/documentParser.js", this);

    documentSections = new ArrayList<>();
    tabDrawerAdapter = new TabDrawerAdapter(mWebViews);


    Intent i = getIntent();
    if (i.getBooleanExtra(EXTRA_LIBRARY, false)) {
      manageZimFiles(2);
    }
    if (i.hasExtra(TAG_FILE_SEARCHED)) {
      searchForTitle(i.getStringExtra(TAG_FILE_SEARCHED));
      selectTab(mWebViews.size() - 1);
    }
    if (i.hasExtra(EXTRA_CHOSE_X_URL)) {
      newTab();
      getCurrentWebView().loadUrl(i.getStringExtra(EXTRA_CHOSE_X_URL));
    }
    if (i.hasExtra(EXTRA_CHOSE_X_TITLE)) {
      newTab();
      getCurrentWebView().loadUrl(i.getStringExtra(EXTRA_CHOSE_X_TITLE));
    }
    if (i.hasExtra(EXTRA_ZIM_FILE)) {
      File file = new File(FileUtils.getFileName(i.getStringExtra(EXTRA_ZIM_FILE)));
      LibraryFragment.mService.cancelNotification(i.getIntExtra(EXTRA_NOTIFICATION_ID, 0));
      Uri uri = Uri.fromFile(file);

      finish();
      Intent zimFile = new Intent(MainActivity.this, MainActivity.class);
      zimFile.setData(uri);
      startActivity(zimFile);
    }

    wasHideToolbar = isHideToolbar;
  }

  /**
   * Creates a listener, which allows to apply the window insets to the tab switcher's padding.
   *
   * @return The listener, which has been created, as an instance of the type {@link
   * OnApplyWindowInsetsListener}. The listener may not be nullFG
   */
  @NonNull
  private OnApplyWindowInsetsListener createWindowInsetsListener() {
    return (view, insets) -> {
      int left = insets.getSystemWindowInsetLeft();
      int top = insets.getSystemWindowInsetTop();
      int right = insets.getSystemWindowInsetRight();
      int bottom = insets.getSystemWindowInsetBottom();
      tabSwitcher.setPadding(left, top, right, bottom);
      float touchableAreaTop = top;

      if (tabSwitcher.getLayout() == Layout.TABLET) {
        touchableAreaTop += getResources()
            .getDimensionPixelSize(R.dimen.tablet_tab_container_height);
      }

      RectF touchableArea = new RectF(left, touchableAreaTop,
          getDisplayWidth(MainActivity.this) - right, touchableAreaTop +
          ThemeUtil.getDimensionPixelSize(MainActivity.this, R.attr.actionBarSize));
      tabSwitcher.addDragGesture(
          new SwipeGesture.Builder().setTouchableArea(touchableArea).create());
      tabSwitcher.addDragGesture(
          new PullDownGesture.Builder().setTouchableArea(touchableArea).create());
      return insets;
    };
  }

  /**
   * Creates and returns a listener, which allows to add a tab to the activity's tab switcher,
   * when a button is clicked.
   *
   * @return The listener, which has been created, as an instance of the type {@link
   * View.OnClickListener}. The listener may not be null
   */
  @NonNull
  private View.OnClickListener createAddTabListener() {
    return view -> {
      int index = tabSwitcher.getCount();
      de.mrapp.android.tabswitcher.Animation animation = createRevealAnimation();
      tabSwitcher.addTab(createTab(index), 0, animation);
    };
  }

  /**
   * Creates and returns a listener, which allows to observe, when an item of the tab switcher's
   * toolbar has been clicked.
   *
   * @return The listener, which has been created, as an instance of the type {@link
   * Toolbar.OnMenuItemClickListener}. The listener may not be null
   */
  @NonNull
  private Toolbar.OnMenuItemClickListener createToolbarMenuListener() {
    return item -> {
      switch (item.getItemId()) {
        case R.id.remove_tab_menu_item:
          Tab selectedTab = tabSwitcher.getSelectedTab();

          if (selectedTab != null) {
            tabSwitcher.removeTab(selectedTab);
          }

          return true;
        case R.id.add_tab_menu_item:
          int index = tabSwitcher.getCount();
          Tab tab = createTab(index);
          tabSwitcher.addTab(tab, 0, createRevealAnimation());

          return true;
        case R.id.clear_tabs_menu_item:
          tabSwitcher.clear();
          return true;
        default:
          return false;
      }
    };
  }

  /**
   * Inflates the tab switcher's menu.
   */
  private void inflateMenu() {
    tabSwitcher.inflateToolbarMenu(R.menu.tab_switcher, createToolbarMenuListener());
  }

  /**
   * Creates and returns a listener, which allows to toggle the visibility of the tab switcher,
   * when a button is clicked.
   *
   * @return The listener, which has been created, as an instance of the type {@link
   * View.OnClickListener}. The listener may not be null
   */
  @NonNull
  private View.OnClickListener createTabSwitcherButtonListener() {
    return view -> tabSwitcher.toggleSwitcherVisibility();
  }

  /**
   * Creates and returns a listener, which allows to add a new tab to the tab switcher, when the
   * corresponding button is clicked.
   *
   * @return The listener, which has been created, as an instance of the type {@link
   * AddTabButtonListener}. The listener may not be null
   */
  @NonNull
  private AddTabButtonListener createAddTabButtonListener() {
    return tabSwitcher -> {
      int index = tabSwitcher.getCount();
      Tab tab = createTab(index);
      tabSwitcher.addTab(tab, 0);
    };
  }

  /**
   * Creates and returns a listener, which allows to undo the removal of tabs from the tab
   * switcher, when the button of the activity's snackbar is clicked.
   *
   * @param snackbar The activity's snackbar as an instance of the class {@link Snackbar}. The snackbar
   *                 may not be null
   * @param index    The index of the first tab, which has been removed, as an {@link Integer} value
   * @param tabs     An array, which contains the tabs, which have been removed, as an array of the type
   *                 {@link Tab}. The array may not be null
   * @return The listener, which has been created, as an instance of the type {@link
   * View.OnClickListener}. The listener may not be null
   */
  @NonNull
  private View.OnClickListener createUndoSnackbarListener(@NonNull final Snackbar snackbar,
                                                          final int index,
                                                          @NonNull final Tab... tabs) {
    return view -> {
      snackbar.setAction(null, null);

      if (tabSwitcher.isSwitcherShown()) {
        tabSwitcher.addAllTabs(tabs, index);
      } else if (tabs.length == 1) {
        tabSwitcher.addTab(tabs[0], 0, createPeekAnimation());
      }

    };
  }

  /**
   * Creates and returns a callback, which allows to observe, when a snackbar, which allows to
   * undo the removal of tabs, has been dismissed.
   *
   * @param tabs An array, which contains the tabs, which have been removed, as an array of the type
   *             {@link Tab}. The tab may not be null
   * @return The callback, which has been created, as an instance of the type class {@link
   * BaseTransientBottomBar.BaseCallback}. The callback may not be null
   */
  @NonNull
  private BaseTransientBottomBar.BaseCallback<Snackbar> createUndoSnackbarCallback(
      final Tab... tabs) {
    return new BaseTransientBottomBar.BaseCallback<Snackbar>() {

      @Override
      public void onDismissed(final Snackbar snackbar, final int event) {
        if (event != DISMISS_EVENT_ACTION) {
          for (Tab tab : tabs) {
            tabSwitcher.clearSavedState(tab);
          }
        }
      }
    };
  }

  /**
   * Shows a snackbar, which allows to undo the removal of tabs from the activity's tab switcher.
   *
   * @param text  The text of the snackbar as an instance of the type {@link CharSequence}. The text
   *              may not be null
   * @param index The index of the first tab, which has been removed, as an {@link Integer} value
   * @param tabs  An array, which contains the tabs, which have been removed, as an array of the type
   *              {@link Tab}. The array may not be null
   */
  private void showUndoSnackbar(@NonNull final CharSequence text, final int index,
                                @NonNull final Tab... tabs) {
    snackbar = Snackbar.make(tabSwitcher, text, Snackbar.LENGTH_LONG).setActionTextColor(
        ContextCompat.getColor(this, R.color.accent));
    snackbar.setAction(R.string.undo, createUndoSnackbarListener(snackbar, index, tabs));
    snackbar.addCallback(createUndoSnackbarCallback(tabs));
    snackbar.show();
  }

  /**
   * Creates a reveal animation, which can be used to add a tab to the activity's tab switcher.
   *
   * @return The reveal animation, which has been created, as an instance of the class {@link
   * de.mrapp.android.tabswitcher.Animation}. The animation may not be null
   */
  @NonNull
  private de.mrapp.android.tabswitcher.Animation createRevealAnimation() {
    float x = 0;
    float y = 0;
    View view = getNavigationMenuItem();

    if (view != null) {
      int[] location = new int[2];
      view.getLocationInWindow(location);
      x = location[0] + (view.getWidth() / 2f);
      y = location[1] + (view.getHeight() / 2f);
    }

    return new RevealAnimation.Builder().setX(x).setY(y).create();
  }

  /**
   * Creates a peek animation, which can be used to add a tab to the activity's tab switcher.
   *
   * @return The peek animation, which has been created, as an instance of the class {@link
   * de.mrapp.android.tabswitcher.Animation}. The animation may not be null
   */
  @NonNull
  private de.mrapp.android.tabswitcher.Animation createPeekAnimation() {
    return new PeekAnimation.Builder().setX(tabSwitcher.getWidth() / 2f).create();
  }

  /**
   * Returns the menu item, which shows the navigation icon of the tab switcher's toolbar.
   *
   * @return The menu item, which shows the navigation icon of the tab switcher's toolbar, as an
   * instance of the class {@link View} or null, if no navigation icon is shown
   */
  @Nullable
  private View getNavigationMenuItem() {
    Toolbar[] toolbars = tabSwitcher.getToolbars();

    if (toolbars != null) {
      Toolbar toolbar = toolbars.length > 1 ? toolbars[1] : toolbars[0];
      int size = toolbar.getChildCount();

      for (int i = 0; i < size; i++) {
        View child = toolbar.getChildAt(i);

        if (child instanceof ImageButton) {
          return child;
        }
      }
    }

    return null;
  }

  /**
   * Creates and returns a tab.
   *
   * @param index The index, the tab should be added at, as an {@link Integer} value
   * @return The tab, which has been created, as an instance of the class {@link Tab}. The tab may
   * not be null
   */
  @NonNull
  private Tab createTab(final int index) {
    CharSequence title = getString(R.string.tab_title, index + 1);
    Tab tab = new Tab(title);
    Bundle parameters = new Bundle();
    parameters.putInt(VIEW_TYPE_EXTRA, index % 3);
    tab.setParameters(parameters);
    return tab;
  }

  @Override
  public final void onSwitcherShown(@NonNull final TabSwitcher tabSwitcher) {

  }

  @Override
  public final void onSwitcherHidden(@NonNull final TabSwitcher tabSwitcher) {
    if (snackbar != null) {
      snackbar.dismiss();
    }
  }

  @Override
  public final void onSelectionChanged(@NonNull final TabSwitcher tabSwitcher,
                                       final int selectedTabIndex,
                                       @Nullable final Tab selectedTab) {

  }

  @Override
  public final void onTabAdded(@NonNull final TabSwitcher tabSwitcher, final int index,
                               @NonNull final Tab tab, @NonNull final de.mrapp.android.tabswitcher.Animation animation) {
    inflateMenu();
    TabSwitcher.setupWithMenu(tabSwitcher, createTabSwitcherButtonListener());
  }

  @Override
  public final void onTabRemoved(@NonNull final TabSwitcher tabSwitcher, final int index,
                                 @NonNull final Tab tab, @NonNull final de.mrapp.android.tabswitcher.Animation animation) {
    CharSequence text = getString(R.string.removed_tab_snackbar, tab.getTitle());
    showUndoSnackbar(text, index, tab);
    inflateMenu();
    TabSwitcher.setupWithMenu(tabSwitcher, createTabSwitcherButtonListener());
  }

  @Override
  public final void onAllTabsRemoved(@NonNull final TabSwitcher tabSwitcher,
                                     @NonNull final Tab[] tabs,
                                     @NonNull final de.mrapp.android.tabswitcher.Animation animation) {
    CharSequence text = getString(R.string.cleared_tabs_snackbar);
    showUndoSnackbar(text, 0, tabs);
    inflateMenu();
    TabSwitcher.setupWithMenu(tabSwitcher, createTabSwitcherButtonListener());
  }


  /**
   * The decorator, which is used to inflate and visualize the tabs of the activity's tab
   * switcher.
   */
  private class Decorator extends TabSwitcherDecorator {

    @NonNull
    @Override
    public View onInflateView(@NonNull final LayoutInflater inflater,
                              @Nullable final ViewGroup parent, final int viewType) {
      View view = inflater.inflate(R.layout.main, parent, false);

      toolbar = view.findViewById(R.id.toolbar);
      toolbar.inflateMenu(R.menu.menu_main);
      toolbar.setOnMenuItemClickListener(createToolbarMenuListener());
      Menu menu = toolbar.getMenu();
      TabSwitcher.setupWithMenu(tabSwitcher, menu, createTabSwitcherButtonListener());
      backToTopButton = view.findViewById(R.id.button_backtotop);
      stopTTSButton = view.findViewById(R.id.button_stop_tts);
      pauseTTSButton = view.findViewById(R.id.button_pause_tts);
      TTSControls = view.findViewById(R.id.tts_controls);
      toolbarContainer = view.findViewById(R.id.toolbar_layout);
      progressBar = view.findViewById(R.id.progress_view);
      exitFullscreenButton = view.findViewById(R.id.FullscreenControlButton);
      snackbarLayout = view.findViewById(R.id.snackbar_layout);
      newTabButton = view.findViewById(R.id.new_tab_button);
      drawerLayout = view.findViewById(R.id.drawer_layout);
      tabDrawerLeftContainer = view.findViewById(R.id.left_drawer);
      tableDrawerRightContainer = view.findViewById(R.id.right_drawer);
      tabDrawerLeft = view.findViewById(R.id.left_drawer_list);
      tableDrawerRight = view.findViewById(R.id.right_drawer_list);
      contentFrame = view.findViewById(R.id.content_frame);
      tabBackButton = view.findViewById(R.id.action_back_button);
      tabForwardButton = view.findViewById(R.id.action_forward_button);
      tabBackButtonContainer = view.findViewById(R.id.action_back);
      tabForwardButtonContainer = view.findViewById(R.id.action_forward);
      pageBottomTabLayout = view.findViewById(R.id.page_bottom_tab_layout);

      newTabButton.setOnClickListener((v) -> newTab());
      tabForwardButtonContainer.setOnClickListener((v) -> {
        if (getCurrentWebView().canGoForward()) {
          getCurrentWebView().goForward();
        }
      });
      tabBackButtonContainer.setOnClickListener((v) -> {
        if (getCurrentWebView().canGoBack()) {
          getCurrentWebView().goBack();
        }
      });

      tabDrawerLeft.setLayoutManager(new LinearLayoutManager(MainActivity.this));
      tabDrawerLeft.setAdapter(tabDrawerAdapter);
      tableDrawerRight.setLayoutManager(new LinearLayoutManager(MainActivity.this));

      TableDrawerAdapter tableDrawerAdapter = new TableDrawerAdapter();
      tableDrawerRight.setAdapter(tableDrawerAdapter);
      tableDrawerAdapter.setTableClickListener(new TableClickListener() {
        @Override
        public void onHeaderClick(View view) {
          getCurrentWebView().setScrollY(0);
          drawerLayout.closeDrawer(GravityCompat.END);
        }

        @Override
        public void onSectionClick(View view, int position) {
          getCurrentWebView().loadUrl("javascript:document.getElementById('"
              + documentSections.get(position).id
              + "').scrollIntoView();");

          drawerLayout.closeDrawers();
        }
      });

      tableDrawerAdapter.notifyDataSetChanged();

      tabDrawerAdapter.setTabClickListener(new TabDrawerAdapter.TabClickListener() {
        @Override
        public void onSelectTab(View view, int position) {
          selectTab(position);

          /* Bug Fix
           * Issue #592 in which the navigational history of the previously open tab (WebView) was
           * carried forward to the newly selected/opened tab; causing erroneous enabling of
           * navigational buttons.
           */
          refreshNavigationButtons();
        }

        @Override
        public void onCloseTab(View view, int position) {
          closeTab(position);
        }
      });


      return view;
    }

    @Override
    public void onShowTab(@NonNull final Context context,
                          @NonNull final TabSwitcher tabSwitcher, @NonNull final View view,
                          @NonNull final Tab tab, final int index, final int viewType,
                          @Nullable final Bundle savedInstanceState) {
      TextView textView = findViewById(android.R.id.title);
      textView.setText(tab.getTitle());
      toolbar = findViewById(R.id.toolbar);
      toolbar.setVisibility(tabSwitcher.isSwitcherShown() ? View.GONE : View.VISIBLE);
    }
  }
}