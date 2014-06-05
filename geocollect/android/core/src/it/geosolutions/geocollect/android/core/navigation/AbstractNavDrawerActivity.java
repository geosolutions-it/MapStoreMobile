package it.geosolutions.geocollect.android.core.navigation;

import it.geosolutions.geocollect.android.core.R;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public abstract class AbstractNavDrawerActivity extends SherlockFragmentActivity {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    
    private ListView mDrawerList;
    
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    
    private NavDrawerActivityConfiguration navConf ;
    
    protected abstract NavDrawerActivityConfiguration getNavDrawerConfiguration();
    
    protected abstract void onNavItemSelected( int id );
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        navConf = getNavDrawerConfiguration();
        
        setContentView(navConf.getMainLayout()); 
        
        mTitle = mDrawerTitle = getTitle();
        
        mDrawerLayout = (DrawerLayout) findViewById(navConf.getDrawerLayoutId());
        mDrawerList = (ListView) findViewById(navConf.getLeftDrawerId());
        mDrawerList.setAdapter(navConf.getBaseAdapter());
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        
        
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                getDrawerIcon(),
                navConf.getDrawerOpenDesc(),
                navConf.getDrawerCloseDesc()
                ) {
            public void onDrawerClosed(View view) {
            	getSupportActionBar().setTitle(mTitle);
            	supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
            	getSupportActionBar().setTitle(mDrawerTitle);
            	supportInvalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }
    
   
    
    protected int getDrawerIcon() {
        return R.drawable.ic_drawer;
    }
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if ( navConf.getActionMenuItemsToHideWhenDrawerOpen() != null ) {
            boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
            for( int iItem : navConf.getActionMenuItemsToHideWhenDrawerOpen()) {
                menu.findItem(iItem).setVisible(!drawerOpen);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

    	if (item.getItemId() == android.R.id.home) {

            if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
            	mDrawerLayout.closeDrawer(mDrawerList);
            } else {
            	mDrawerLayout.openDrawer(mDrawerList);
            	
            }
        //layer menu part
		}

        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ( keyCode == KeyEvent.KEYCODE_MENU ) {
            if ( this.mDrawerLayout.isDrawerOpen(this.mDrawerList)) {
                this.mDrawerLayout.closeDrawer(this.mDrawerList);
            }
            else {
                this.mDrawerLayout.openDrawer(this.mDrawerList);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    protected DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

    protected ActionBarDrawerToggle getDrawerToggle() {
        return mDrawerToggle;
    }
   
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }
    
    public void selectItem(int position) {
        NavDrawerItem selectedItem = navConf.getNavItems()[position];
        
        this.onNavItemSelected(selectedItem.getId());
        mDrawerList.setItemChecked(position, true);
        
        if ( selectedItem.updateActionBarTitle()) {
            setTitle(selectedItem.getLabel());
        }
        
        if ( this.mDrawerLayout.isDrawerOpen(this.mDrawerList)) {
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }
    
    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }
}