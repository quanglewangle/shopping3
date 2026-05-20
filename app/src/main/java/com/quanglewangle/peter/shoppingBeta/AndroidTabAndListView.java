package com.quanglewangle.peter.shopping3;


import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import com.quanglewangle.peter.shoppingBeta.R;

public class AndroidTabAndListView extends TabActivity {
	// TabSpec Names
	private static final String INBOX_SPEC = "Cupboard";
	private static final String OUTBOX_SPEC = "List";
	private static final String PROFILE_SPEC = "Basket";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        TabHost tabHost = getTabHost();
        // Inbox Tab
        TabSpec inboxSpec = tabHost.newTabSpec(INBOX_SPEC);
        // Tab Icon
        inboxSpec.setIndicator(INBOX_SPEC, getResources().getDrawable(R.drawable.icon_inbox));
        Intent inboxIntent = new Intent(this, CupboardActivity.class);
        inboxIntent.putExtra("firstKeyName","FirstKeyValue");
        // Tab Content
        inboxSpec.setContent(inboxIntent);

        // Outbox Tab
        TabSpec outboxSpec = tabHost.newTabSpec(OUTBOX_SPEC);
        outboxSpec.setIndicator(OUTBOX_SPEC, getResources().getDrawable(R.drawable.icon_outbox));
        Intent outboxIntent = new Intent(this, ListActivity.class);
        outboxSpec.setContent(outboxIntent);
        
        // Profile Tab
        TabSpec profileSpec = tabHost.newTabSpec(PROFILE_SPEC);
        profileSpec.setIndicator(PROFILE_SPEC, getResources().getDrawable(R.drawable.icon_profile));
        Intent profileIntent = new Intent(this, BasketActivity.class);
        profileSpec.setContent(profileIntent);
        
        // Adding all TabSpec to TabHost
        tabHost.addTab(inboxSpec); // Adding Inbox tab
        tabHost.addTab(outboxSpec); // Adding Outbox tab
        tabHost.addTab(profileSpec); // Adding Profile tab
    }
}