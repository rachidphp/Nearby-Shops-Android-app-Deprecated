package org.nearbyshops.enduserappnew;


import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;

import org.nearbyshops.enduserappnew.Lists.CartsList.CartsListFragment;
import org.nearbyshops.enduserappnew.Interfaces.*;
import org.nearbyshops.enduserappnew.Lists.ItemsByCategory.ItemsByCatFragment;
import org.nearbyshops.enduserappnew.Login.LoginPlaceholder.FragmentSignInMessage;
import org.nearbyshops.enduserappnew.Lists.Markets.Interfaces.MarketSelected;
import org.nearbyshops.enduserappnew.Lists.Markets.MarketsFragmentNew;
import org.nearbyshops.enduserappnew.OneSignal.PrefOneSignal;
import org.nearbyshops.enduserappnew.OneSignal.UpdateOneSignalID;
import org.nearbyshops.enduserappnew.Lists.OrderHistory.OrdersHistoryFragment;
import org.nearbyshops.enduserappnew.Preferences.PrefGeneral;
import org.nearbyshops.enduserappnew.Preferences.PrefLocation;
import org.nearbyshops.enduserappnew.Preferences.PrefLogin;
import org.nearbyshops.enduserappnew.Preferences.PrefServiceConfig;
import org.nearbyshops.enduserappnew.Services.UpdateServiceConfiguration;
import org.nearbyshops.enduserappnew.Lists.ShopsList.FragmentShopsList;
import org.nearbyshops.enduserappnew.Utility.UtilityFunctions;


public class Home extends AppCompatActivity implements ShowFragment, NotifyAboutLogin, MarketSelected {



    public static final String TAG_LOGIN = "tag_login";
    public static final String TAG_PROFILE = "tag_profile_fragment";

    public static final String TAG_ITEMS_FRAGMENT = "tag_items_fragment";
    public static final String TAG_SHOPS_FRAGMENT = "tag_shops_fragment";
    public static final String TAG_CARTS_FRAGMENT = "tag_carts_fragment";
    public static final String TAG_ORDERS_FRAGMENT = "tag_orders_fragment";

    public static final String TAG_MARKET_FRAGMENT = "tag_market_fragment";


    private static final int REQUEST_CHECK_SETTINGS = 100;



    BottomNavigationView bottomBar;

    LocationManager locationManager;
    LocationListener locationListener;




    public Home() {

        DaggerComponentBuilder.getInstance()
                .getNetComponent()
                .Inject(this);
    }





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_new);

        bottomBar = findViewById(R.id.bottom_navigation);

//        bottomBar.setDefaultTab(R.id.tab_search);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);



        startService(new Intent(this,UpdateServiceConfiguration.class));



        FirebaseApp.initializeApp(getApplicationContext());
        UtilityFunctions.updateFirebaseSubscriptions();





        if (PrefGeneral.getMultiMarketMode(this)) {

            bottomBar.getMenu().getItem(4).setTitle("Markets");

//            bottomBar.getTabWithId(R.id.tab_profile).setTitle("Markets");
        }
        else
        {

            bottomBar.getMenu().getItem(4).setTitle("Profile");


//            bottomBar.getTabWithId(R.id.tab_profile).setTitle("Profile");
        }



        bottomBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {


                if(menuItem.getItemId()== R.id.bottom_tab_items)
                {
                    showItemsFragment();
                }
                else if(menuItem.getItemId()== R.id.bottom_tab_shops)
                {
                    showShopsFragment();
                }
                else if(menuItem.getItemId()== R.id.bottom_tab_cart)
                {
                    showCartFragment();
                }
                else if(menuItem.getItemId()== R.id.bottom_tab_orders)
                {
                    showOrdersFragment();
                }
                else if(menuItem.getItemId()== R.id.bottom_tab_profile)
                {
                    showProfileFragment(false);
                }


                return true;
            }
        });


//        bottomBar.setSelectedItemId(R.id.bottom_tab_items);




        if(savedInstanceState==null)
        {
//            showToast("Home : OnSaveInstanceState");
            initialFragmentSetup();
        }



//        startSettingsCheck();



        checkPermissions();
        fetchLocation();




        if (PrefGeneral.getServiceURL(this) != null
                && PrefOneSignal.getToken(this) != null
                && PrefLogin.getUser(this)!=null) {

                startService(new Intent(getApplicationContext(), UpdateOneSignalID.class));
        }





        if (PrefServiceConfig.getServiceConfigLocal(this) == null && PrefGeneral.getServiceURL(this) != null) {
            // get service configuration when its null ... fetches config at first install or changing service
            startService(new Intent(getApplicationContext(), UpdateServiceConfiguration.class));
        }






        setupBottomBarLights();



    }




    void setupBottomBarLights()
    {
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {

                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

                if(fragment instanceof MarketsFragmentNew)
                {
//                    bottomBar.selectTabWithId(R.id.tab_profile);
//                    bottomBar.setSelectedItemId(R.id.tab_profile);

                    bottomBar.getMenu().getItem(4).setChecked(true);
                }
                else if(fragment instanceof ProfileFragment)
                {
                    bottomBar.getMenu().getItem(4).setChecked(true);
                }
                else if(fragment instanceof OrdersHistoryFragment)
                {
                    bottomBar.getMenu().getItem(3).setChecked(true);

                }
                else if(fragment instanceof CartsListFragment)
                {
                    bottomBar.getMenu().getItem(2).setChecked(true);
                }
                else if(fragment instanceof FragmentShopsList)
                {
                    bottomBar.getMenu().getItem(1).setChecked(true);
                }
                else if(fragment instanceof ItemsByCatFragment)
                {

                    bottomBar.getMenu().getItem(0).setChecked(true);

                }
                else if(fragment instanceof FragmentSignInMessage)
                {
//                    bottomBar.getMenu().getItem(3).setChecked(true);
                }



            }
        });
    }




    void showLogMessage(String message) {
        Log.d("log_home_screen", message);
    }




    @Override
    public void loginSuccess() {


        getSupportFragmentManager().popBackStackImmediate(
                null,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
        );

        marketSelected();
    }




    @Override
    public void loggedOut() {
        showProfileFragment(true);
    }







    @Override
    public void showLoginFragment() {


        if(getSupportFragmentManager().findFragmentByTag(TAG_LOGIN)==null)
        {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new FragmentSignInMessage(), TAG_LOGIN)
                    .commit();
        }

    }





    void checkPermissions() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.


            //
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    2);
            return;
        }


//        fetchLocation();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        //
//        if(requestCode==2)
//        {
//            // If request is cancelled, the result arrays are empty.
//
//
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // permission was granted, yay! Do the
            // contacts-related task you need to do.
            //                startService(new Intent(this,LocationUpdateServiceLocal.class));

            showToastMessage("Permission Granted !");

//            Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_ITEMS_FRAGMENT);


//            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
//
//
//            if (fragment instanceof LocationUpdated) {
//                ((LocationUpdated) fragment).permissionGranted();
//            }


            fetchLocation();

        } else {
            // permission denied, boo! Disable the
            // functionality that depends on this permission.
            showToastMessage("Permission Rejected");
        }


    }






    @Override
    public void showProfileFragment(boolean refreshFragment) {

        if (PrefGeneral.getMultiMarketMode(this)) {
            // no market selected therefore show available markets in users area


            if(refreshFragment)
            {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new MarketsFragmentNew(), TAG_MARKET_FRAGMENT)
                        .commit();
            }
            else
            {

                if (getSupportFragmentManager().findFragmentByTag(TAG_MARKET_FRAGMENT) == null) {

                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new MarketsFragmentNew(), TAG_MARKET_FRAGMENT)
                            .commit();
                }
            }


        } else {
            // single market mode

            if (PrefLogin.getUser(getBaseContext()) == null) {


                showLoginFragment();

            }
            else {



                if(getSupportFragmentManager().findFragmentByTag(TAG_PROFILE)==null)
                {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new ProfileFragment(), TAG_PROFILE)
                            .commit();
                }


            }

        }

    }



    @Override
    public void showOrdersFragment() {




        if (PrefGeneral.getMultiMarketMode(this) && PrefGeneral.getServiceURL(this) == null) {
            // no market selected therefore show available markets in users area


            if (getSupportFragmentManager().findFragmentByTag(TAG_MARKET_FRAGMENT) == null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new MarketsFragmentNew(), TAG_MARKET_FRAGMENT)
                        .commit();

            }


//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.fragment_container, new MarketsFragmentNew(), TAG_MARKET_FRAGMENT)
//                    .addToBackStack(null)
//                    .commit();


        } else if (PrefLogin.getUser(getBaseContext()) == null) {

            showLoginFragment();

        }
        else {



            if(getSupportFragmentManager().findFragmentByTag(TAG_ORDERS_FRAGMENT)==null)
            {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, OrdersHistoryFragment.newInstance(true,false,false), TAG_ORDERS_FRAGMENT)
                        .commitNow();

            }
        }
    }



    @Override
    public void showCartFragment() {


        if (PrefGeneral.getMultiMarketMode(this) && PrefGeneral.getServiceURL(this) == null) {
            // no market selected therefore show available markets in users area


            if (getSupportFragmentManager().findFragmentByTag(TAG_MARKET_FRAGMENT) == null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new MarketsFragmentNew(), TAG_MARKET_FRAGMENT)
                        .commitNow();

            }

//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.fragment_container, new MarketsFragmentNew(), TAG_MARKET_FRAGMENT)
//                    .addToBackStack(null)
//                    .commit();



        } else if (PrefLogin.getUser(getBaseContext()) == null) {

            showLoginFragment();
            return;

        }

        else {


            if(getSupportFragmentManager().findFragmentByTag(TAG_CARTS_FRAGMENT)==null)
            {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new CartsListFragment(), TAG_CARTS_FRAGMENT)
                        .commitNow();

            }



//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.fragment_container, new CartsListFragment(), TAG_CARTS_FRAGMENT)
//                    .addToBackStack(null)
//                    .commit();



        }
    }





//    Fragment fragmentShop;

    @Override
    public void showShopsFragment() {


        if (PrefGeneral.getMultiMarketMode(this) && PrefGeneral.getServiceURL(this) == null) {
            // no market selected therefore show available markets in users area




            if (getSupportFragmentManager().findFragmentByTag(TAG_MARKET_FRAGMENT) == null) {

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new MarketsFragmentNew(), TAG_MARKET_FRAGMENT)
                        .commitNow();

            }


//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.fragment_container, new MarketsFragmentNew(), TAG_MARKET_FRAGMENT)
//                    .addToBackStack(null)
//                    .commit();



        } else {



//            else
//            {
//                getSupportFragmentManager()
//                        .beginTransaction()
//                        .show(getSupportFragmentManager().findFragmentByTag(TAG_SHOPS_FRAGMENT));
//            }







//
//
//            if(!getSupportFragmentManager().popBackStackImmediate("shop_fragment",0))
//            {
//                getSupportFragmentManager()
//                        .beginTransaction()
//                        .replace(R.id.fragment_container, FragmentShopNewBackup.newInstance(false), TAG_SHOPS_FRAGMENT)
//                        .addToBackStack("shop_fragment")
//                        .commit();
//            }



//
//            if(fragmentShop==null)
//            {
//                fragmentShop = FragmentShopNewBackup.newInstance(false);
//            }






            if (getSupportFragmentManager().findFragmentByTag(TAG_SHOPS_FRAGMENT) == null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, FragmentShopsList.newInstance(false), TAG_SHOPS_FRAGMENT)
                        .commitNow();
            }

//
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.fragment_container, FragmentShopsList.newInstance(false), TAG_SHOPS_FRAGMENT)
//                    .addToBackStack("shop_fragment")
//                    .commit();



        }



    }


    @Override
    public void showItemsFragment() {



//        showToast("Show Items Triggered !");


        if (PrefGeneral.getMultiMarketMode(this) && PrefGeneral.getServiceURL(this) == null) {
            // no market selected therefore show available markets in users area


            if (getSupportFragmentManager().findFragmentByTag(TAG_MARKET_FRAGMENT) == null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new MarketsFragmentNew(), TAG_MARKET_FRAGMENT)
                        .commitNow();

            }


//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.fragment_container, new MarketsFragmentNew(), TAG_MARKET_FRAGMENT)
//                    .addToBackStack(null)
//                    .commit();

        }
        else {



            if (getSupportFragmentManager().findFragmentByTag(TAG_ITEMS_FRAGMENT) == null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new ItemsByCatFragment(), TAG_ITEMS_FRAGMENT)
                        .commitNow();
            }

//            else {
//                getSupportFragmentManager().popBackStack();
//            }


//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.fragment_container, new ItemsByCatFragment(), TAG_ITEMS_FRAGMENT)
//                    .addToBackStack("items")
//                    .commit();






        }
    }





    void initialFragmentSetup()
    {
        if (PrefGeneral.getMultiMarketMode(this) && PrefGeneral.getServiceURL(this) == null) {
            // no market selected therefore show available markets in users area



            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new MarketsFragmentNew(), TAG_MARKET_FRAGMENT)
                    .commitNow();


        }
        else {

//            new ItemsDatabaseForAdminFragment()


            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, FragmentShopsList.newInstance(false), TAG_SHOPS_FRAGMENT)
                    .commitNow();


        }
    }






    void showToastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    boolean isDestroyed = false;


    @Override
    public void onBackPressed() {



//        Fragment fragment = getSupportFragmentManager()
//                .findFragmentByTag(TAG_ITEMS_FRAGMENT);
//
//
//        if (fragment == null) {
//            fragment = getSupportFragmentManager()
//                    .findFragmentByTag(TAG_SHOPS_FRAGMENT);
//        }


        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);


        if (fragment instanceof NotifyBackPressed) {
//            showLogMessage("Fragment Instanceof NotifyBackPressed !");

            if (((NotifyBackPressed) fragment).backPressed()) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }







    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_items_by_cat_simple, menu);


        // Associate searchable configuration with the SearchView
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));


        MenuItem item = menu.findItem(R.id.action_search);

        item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {

                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {

                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

                if (fragment instanceof NotifySearch) {
                    ((NotifySearch) fragment).endSearchMode();
                }

//                Toast.makeText(Home.this, "onCollapsed Called ", Toast.LENGTH_SHORT).show();

                return true;
            }
        });


        return true;
    }







    void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleIntent(intent);
    }


    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search your data somehow

//            Toast.makeText(this,query,Toast.LENGTH_SHORT).show();


            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);


            if (fragment instanceof NotifySearch) {
                ((NotifySearch) fragment).search(query);
            }
        }
    }





    @Override
    public void marketSelected() {

//            bottomBar.selectTabWithId(R.id.tab_items);
//            bottomBar.selectTabAtPosition(bottomBar.getCurrentTabPosition());
//            showItemsFragment();


//        showToastMessage("Market Selected : Home ");



        getSupportFragmentManager().popBackStackImmediate(
                null,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
        );



        int tabId = bottomBar.getSelectedItemId();


        if (tabId == R.id.bottom_tab_items) {
            showItemsFragment();
        } else if (tabId == R.id.bottom_tab_shops) {

            showShopsFragment();
        } else if (tabId == R.id.bottom_tab_cart) {

            showCartFragment();
        } else if (tabId == R.id.bottom_tab_orders) {

            showOrdersFragment();

        } else if (tabId == R.id.bottom_tab_profile) {

//                showProfileFragment();
//                showItemsFragment();
            bottomBar.setSelectedItemId(R.id.bottom_tab_shops);

        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();

        PrefLocation.setLocationSetByUser(false,this);
    }






    private com.google.android.gms.location.LocationRequest mLocationRequestTwo;
    private com.google.android.gms.location.LocationCallback locationCallback;



    void fetchLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            showToastMessage("We cannot access Location ... Please grant permission for Location access !");

            return;
        }




        mLocationRequestTwo = com.google.android.gms.location.LocationRequest.create();
        mLocationRequestTwo.setInterval(1000);
        mLocationRequestTwo.setSmallestDisplacement(10);
        mLocationRequestTwo.setFastestInterval(1000);
        mLocationRequestTwo.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


//        locationCallback = new MyLocationCallback();

        locationCallback = new com.google.android.gms.location.LocationCallback(){
            @Override
            public void onLocationResult(com.google.android.gms.location.LocationResult locationResult) {
                super.onLocationResult(locationResult);

                if(Home.this.isDestroyed())
                {
                    return;
                }

                double lat = 0;
                double lon = 0;

                Location location = locationResult.getLocations().get(locationResult.getLocations().size()-1);

                lat = location.getLatitude();
                lon = location.getLongitude();



                PrefLocation.saveLatLonCurrent(lat,lon,Home.this);

                saveLocation(location);


                stopLocationUpdates();

//                showToastMessage("Location Updated !");


            }


            @Override
            public void onLocationAvailability(com.google.android.gms.location.LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
            }
        };






        com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(this)
                .getLastLocation()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {


                        com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(Home.this)
                                .requestLocationUpdates(mLocationRequestTwo,locationCallback, null);


                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {


                        if(location==null)
                        {

                            com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(Home.this)
                                    .requestLocationUpdates(mLocationRequestTwo,locationCallback, null);

                            return;
                        }

                        saveLocation(location);
                    }
                });



    }




    void saveLocation(Location location)
    {


        Location currentLocation = PrefLocation.getLocation(this);


//        showToast("Distance Change : " + currentLocation.distanceTo(location));

        if(currentLocation.distanceTo(location)>100)
        {
            // save location only if there is a significant change in location

            PrefLocation.saveLatitude((float) location.getLatitude(), Home.this);
            PrefLocation.saveLongitude((float) location.getLongitude(), Home.this);


//        showToast("Home : Location Updated");

            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

            if (fragment instanceof LocationUpdated) {
                ((LocationUpdated) fragment).locationUpdated();
            }

        }

    }





    protected void stopLocationUpdates() {

        if(locationCallback!=null)
        {
            com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(Home.this)
                    .removeLocationUpdates(locationCallback);
        }
    }


}
