package in.foodmash.app;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;
import butterknife.Bind;
import butterknife.ButterKnife;
import in.foodmash.app.commons.Alerts;
import in.foodmash.app.commons.Animations;
import in.foodmash.app.commons.Swift;
import in.foodmash.app.commons.VolleyFailureFragment;
import in.foodmash.app.utils.WordUtils;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Zeke on Feb 16, 2016.
 */
public class LegaleseActivity extends AppCompatActivity {

    @Bind(R.id.fragment_container) FrameLayout fragmentContainer;
    @Bind(R.id.html_content) TextView htmlContent;
    @Bind(R.id.title) TextView title;
    @Bind(R.id.toolbar) Toolbar toolbar;

    public enum Legalese { TERMS_AND_CONDITIONS, PRIVACY_POLICY, REFUND_POLICY};
    private JsonObjectRequest legaleseRequest;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legalese);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) { e.printStackTrace(); }

        final Legalese legalese = (Legalese) getIntent().getSerializableExtra("Type");

        String string = "<p><b>General Information</b></p>\n" +
                "                <p>Country: India</p>\n" +
                "                <p>State: Tamil Nadu</p>\n" +
                "                <p>Applicability of Terms and Conditions: Website, Mobile App</p>\n" +
                "                <p><b>Business Information</b></p>\n" +
                "                <p>Website URL: <a href=\"http://www.foodmash.in/\">http://www.foodmash.in</a></p>\n" +
                "                <p>Website Name: Foodmash</p>\n" +
                "                <p>Mobile App Name: Foodmash</p>\n" +
                "                <p><b>Company Information</b></p>\n" +
                "                <p>Owned by MEALS ON WHEELS TECHNOLOGY LLP registered by the Registrar of Companies.</p>\n" +
                "                <p><b>Information we collect</b></p>\n" +
                "                <p>1. We sell products, services or subscriptions through our website and mobile app.</p>\n" +
                "                <p><i class=\"fa fa-hand-o-right\"></i> These are physical products that we ship to our customer.</p>\n" +
                "                <p>2. We reserve the right to change the products prices at any time.</p>\n" +
                "                <p>3. We reserve the right to refuse or cancel certain orders at our sole discretion.</p>\n" +
                "                <p>4. We plan to offer contests sweepstakes or promotions on our website and mobile app.</p>\n" +
                "                <p>5. Users can create an account on our website and mobile app.</p>\n" +
                "                <p><i class=\"fa fa-hand-o-right\"></i> Users can have/pick a username to login into our website and mobile app.</p>\n" +
                "                <p><i class=\"fa fa-hand-o-right\"></i> Users cannot create or upload content (text, images, videos etc) on our website and mobile app.</p>\n" +
                "                <p><i class=\"fa fa-hand-o-right\"></i> Users cannot send us copyright infringement notices and we do not respond to copyright infringement notices.</p>\n" +
                "                <p>6. MEALS ON WHEELS TECHNOLOGY LLP reserves the right to terminate access to certain users if these users abuse our website and mobile app.</p>\n" +
                "                <p>7. MEALS ON WHEELS TECHNOLOGY LLP makes it very clear that we own our own content (logo, visual design etc) and trademarks are our exclusive property.</p>\n" +
                "                <p>8. Any material changes to the Terms and Conditions will become effective within a 15 day notice period.</p>\n" +
                "                <p>9. Website and Mobile App will be provided on an “AS IS” and “AS AVAILABLE” basis. “Disclaimer” and “Limitation of Liability” disclosures to be included.</p>\n" +
                "                <p>10. Prices shall be exclusive of delivery cost and all applicable taxes in India</p>\n" +
                "                <p><b>End Users Licence Agreement</b></p>\n" +
                "                <p>The Products transacted through the Service are licensed, not sold, to You for use only under the terms of this license, unless a Product is accompanied by a separate license agreement, in which case the terms of that separate license agreement will govern, subject to Your prior acceptance of that separate license agreement. The licensor (“Application Provider”) reserves all rights not expressly granted to You. The Product that is subject to this license is referred to in this license as the “Licensed Application.”</p>\n" +
                "                <p>a. Scope of License: </p>\n" +
                "                <p>This license granted to You for the Licensed Application by Application Provider is limited to a non-transferable license to use the Licensed Application on any Mobile device that You own or control and as permitted by the Usage Rules set forth in the App / Play Store Terms and Conditions (the “Usage Rules”). This license does not allow You to use the Licensed Application on any mobile device that You do not own or control, and You may not distribute or make the Licensed Application available over a network where it could be used by multiple devices at the same time. You may not rent, lease, lend, sell, redistribute or sublicense the Licensed Application. You may not copy (except as expressly permitted by this license and the Usage Rules), decompile, reverse engineer, disassemble, attempt to derive the source code of, modify, or create derivative works of the Licensed Application, any updates, or any part thereof (except as and only to the extent any foregoing restriction is prohibited by applicable law or to the extent as may be permitted by the licensing terms governing use of any open sourced components included with the Licensed Application). Any attempt to do so is a violation of the rights of the Application Provider and its licensors. If You breach this restriction, You may be subject to prosecution and damages. The terms of the license will govern any upgrades provided by Application Provider that replace and/or supplement the original Product, unless such upgrade is accompanied by a separate license in which case the terms of that license will govern.</p>\n" +
                "                <p>b. Consent to Use of Data: </p>\n" +
                "                <p>You agree that Application Provider may collect and use technical data and related information, including but not limited to technical information about Your device, system and application software, and peripherals, that is gathered periodically to facilitate the provision of software updates, product support and other services to You (if any) related to the Licensed Application. Application Provider may use this information, as long as it is in a form that does not personally identify You, to improve its products or to provide services or technologies to You.</p>\n" +
                "                <p>c. Termination. </p>\n" +
                "                <p>The license is effective until terminated by You or Application Provider. Your rights under this license will terminate automatically without notice from the Application Provider if You fail to comply with any term(s) of this license. Upon termination of the license, You shall cease all use of the Licensed Application, and destroy all copies, full or partial, of the Licensed Application.</p>\n" +
                "                <p>d. Services; </p>\n" +
                "                <p>Third Party Materials. The Licensed Application may enable access to Application Provider’s and third party services and web sites (collectively and individually, \"Services\"). Use of the Services may require Internet access and that You accept additional terms of service.</p>\n" +
                "                <p>You understand that by using any of the Services, You may encounter content that may be deemed offensive, indecent, or objectionable, which content may or may not be identified as having explicit language, and that the results of any search or entering of a particular URL may automatically and unintentionally generate links or references to objectionable material. Nevertheless, You agree to use the Services at Your sole risk and that the Application Provider shall not have any liability to You for content that may be found to be offensive, indecent, or objectionable.</p>\n" +
                "                <p>Certain Services may display, include or make available content, data, information, applications or materials from third parties (“Third Party Materials”) or provide links to certain third party web sites. By using the Services, You acknowledge and agree that the Application Provider is not responsible for examining or evaluating the content, accuracy, completeness, timeliness, validity, copyright compliance, legality, decency, quality or any other aspect of such Third Party Materials or web sites. The Application Provider does not warrant or endorse and does not assume and will not have any liability or responsibility to You or any other person for any third-party Services, Third Party Materials or web sites, or for any other materials, products, or services of third parties. Third Party Materials and links to other web sites are provided solely as a convenience to You. Financial information displayed by any Services is for general informational purposes only and is not intended to be relied upon as investment advice. Before executing any securities transaction based upon information obtained through the Services, You should consult with a financial professional. Location data provided by any Services is for basic navigational purposes only and is not intended to be relied upon in situations where precise location information is needed or where erroneous, inaccurate or incomplete location data may lead to death, personal injury, property or environmental damage. Neither the Application Provider, nor any of its content providers, guarantees the availability, accuracy, completeness, reliability, or timeliness of stock information or location data displayed by any Services.</p>\n" +
                "                <p>You agree that any Services contain proprietary content, information and material that is protected by applicable intellectual property and other laws, including but not limited to copyright, and that You will not use such proprietary content, information or materials in any way whatsoever except for permitted use of the Services. No portion of the Services may be reproduced in any form or by any means. You agree not to modify, rent, lease, loan, sell, distribute, or create derivative works based on the Services, in any manner, and You shall not exploit the Services in any unauthorized way whatsoever, including but not limited to, by trespass or burdening network capacity. You further agree not to use the Services in any manner to harass, abuse, stalk, threaten, defame or otherwise infringe or violate the rights of any other party, and that the Application Provider is not in any way responsible for any such use by You, nor for any harassing, threatening, defamatory, offensive or illegal messages or transmissions that You may receive as a result of using any of the Services.</p>\n" +
                "                <p>In addition, third party Services and Third Party Materials that may be accessed from, displayed on or linked to from the iPhone or iPod touch are not available in all languages or in all countries. The Application Provider makes no representation that such Services and Materials are appropriate or available for use in any particular location. To the extent You choose to access such Services or Materials, You do so at Your own initiative and are responsible for compliance with any applicable laws, including but not limited to applicable local laws. The Application Provider, and its licensors, reserve the right to change, suspend, remove, or disable access to any Services at any time without notice. In no event will the Application Provider be liable for the removal of or disabling of access to any such Services. The Application Provider may also impose limits on the use of or access to certain Services, in any case and without notice or liability.</p>\n" +
                "                <p>e. NO WARRANTY: YOU EXPRESSLY ACKNOWLEDGE AND AGREE THAT USE OF THE LICENSED APPLICATION IS AT YOUR SOLE RISK AND THAT THE ENTIRE RISK AS TO SATISFACTORY QUALITY, PERFORMANCE, ACCURACY AND EFFORT IS WITH YOU. TO THE MAXIMUM EXTENT PERMITTED BY APPLICABLE LAW, THE LICENSED APPLICATION AND ANY SERVICES PERFORMED OR PROVIDED BY THE LICENSED APPLICATION (\"SERVICES\") ARE PROVIDED \"AS IS\" AND “AS AVAILABLE”, WITH ALL FAULTS AND WITHOUT WARRANTY OF ANY KIND, AND APPLICATION PROVIDER HEREBY DISCLAIMS ALL WARRANTIES AND CONDITIONS WITH RESPECT TO THE LICENSED APPLICATION AND ANY SERVICES, EITHER EXPRESS, IMPLIED OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES AND/OR CONDITIONS OF MERCHANTABILITY, OF SATISFACTORY QUALITY, OF FITNESS FOR A PARTICULAR PURPOSE, OF ACCURACY, OF QUIET ENJOYMENT, AND NON-INFRINGEMENT OF THIRD PARTY RIGHTS. APPLICATION PROVIDER DOES NOT WARRANT AGAINST INTERFERENCE WITH YOUR ENJOYMENT OF THE LICENSED APPLICATION, THAT THE FUNCTIONS CONTAINED IN, OR SERVICES PERFORMED OR PROVIDED BY, THE LICENSED APPLICATION WILL MEET YOUR REQUIREMENTS, THAT THE OPERATION OF THE LICENSED APPLICATION OR SERVICES WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT DEFECTS IN THE LICENSED APPLICATION OR SERVICES WILL BE CORRECTED. NO ORAL OR WRITTEN INFORMATION OR ADVICE GIVEN BY APPLICATION PROVIDER OR ITS AUTHORIZED REPRESENTATIVE SHALL CREATE A WARRANTY. SHOULD THE LICENSED APPLICATION OR SERVICES PROVE DEFECTIVE, YOU ASSUME THE ENTIRE COST OF ALL NECESSARY SERVICING, REPAIR OR CORRECTION. SOME JURISDICTIONS DO NOT ALLOW THE EXCLUSION OF IMPLIED WARRANTIES OR LIMITATIONS ON APPLICABLE STATUTORY RIGHTS OF A CONSUMER, SO THE ABOVE EXCLUSION AND LIMITATIONS MAY NOT APPLY TO YOU.</p>\n" +
                "                <p>f. Limitation of Liability. TO THE EXTENT NOT PROHIBITED BY LAW, IN NO EVENT SHALL APPLICATION PROVIDER BE LIABLE FOR PERSONAL INJURY, OR ANY INCIDENTAL, SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES WHATSOEVER, INCLUDING, WITHOUT LIMITATION, DAMAGES FOR LOSS OF PROFITS, LOSS OF DATA, BUSINESS INTERRUPTION OR ANY OTHER COMMERCIAL DAMAGES OR LOSSES, ARISING OUT OF OR RELATED TO YOUR USE OR INABILITY TO USE THE LICENSED APPLICATION, HOWEVER CAUSED, REGARDLESS OF THE THEORY OF LIABILITY (CONTRACT, TORT OR OTHERWISE) AND EVEN IF APPLICATION PROVIDER HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES. SOME JURISDICTIONS DO NOT ALLOW THE LIMITATION OF LIABILITY FOR PERSONAL INJURY, OR OF INCIDENTAL OR CONSEQUENTIAL DAMAGES, SO THIS LIMITATION MAY NOT APPLY TO YOU. In no event shall Application Provider’s total liability to you for all damages (other than as may be required by applicable law in cases involving personal injury) exceed the amount of fifty dollars ($50.00). The foregoing limitations will apply even if the above stated remedy fails of its essential purpose.</p>\n" +
                "                <p>g. You may not use or otherwise export or re-export the Licensed Application except as authorized by United States law and the laws of the jurisdiction in which the Licensed Application was obtained. In particular, but without limitation, the Licensed Application may not be exported or re-exported (a) into any U.S. embargoed countries or (b) to anyone on the U.S. Treasury Department's list of Specially Designated Nationals or the U.S. Department of Commerce Denied Person’s List or Entity List. By using the Licensed Application, you represent and warrant that you are not located in any such country or on any such list. You also agree that you will not use these products for any purposes prohibited by United States law, including, without limitation, the development, design, manufacture or production of nuclear, missiles, or chemical or biological weapons.</p>\n" +
                "                <p>h. The Licensed Application and related documentation are \"Commercial Items\", as that term is defined at 48 C.F.R. §2.101, consisting of \"Commercial Computer Software\" and \"Commercial Computer Software Documentation\", as such terms are used in 48 C.F.R. §12.212 or 48 C.F.R. §227.7202, as applicable. Consistent with 48 C.F.R. §12.212 or 48 C.F.R. §227.7202-1 through 227.7202-4, as applicable, the Commercial Computer Software and Commercial Computer Software Documentation are being licensed to U.S. Government end users (a) only as Commercial Items and (b) with only those rights as are granted to all other end users pursuant to the terms and conditions herein. Unpublished-rights reserved under the copyright laws of the United States.</p>\n" +
                "                <p>i. The laws of the State of California, excluding its conflicts of law rules, govern this license and your use of the Licensed Application. Your use of the Licensed Application may also be subject to other local, state, national, or international laws.</p>";

        htmlContent.setText(Html.fromHtml(string));
        title.setText(WordUtils.titleize(getStringFromLegalese(legalese)));
//
//        legaleseRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_root_path) + "/legalese/" + getStringFromLegalese(legalese), new Response.Listener<JSONObject>() {
//            @Override
//            public void onResponse(JSONObject response) {
//                System.out.println(response);
//                try {
//                    if (response.getBoolean("success")) {
//                        Animations.fadeOut(fragmentContainer,100);
//                        String data = response.getString("data");
//                        htmlContent.setText(Html.fromHtml(data));
//                        System.out.println(data);
//                    } else {
//                        Alerts.requestUnauthorisedAlert(LegaleseActivity.this);
//                        System.out.println(response.getString("error"));
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new VolleyFailureFragment()).commit();
//                getSupportFragmentManager().executePendingTransactions();
//                ((VolleyFailureFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container))
//                        .setJsonObjectRequest(legaleseRequest);
//                System.out.println("Response Error: " + error);
//            }
//        });
//        Animations.fadeIn(fragmentContainer, 300);
//        Swift.getInstance(this).addToRequestQueue(legaleseRequest);
    }

    private String getStringFromLegalese(Legalese legalese) {
        switch (legalese) {
            case TERMS_AND_CONDITIONS: return "termsAndConditions";
            case PRIVACY_POLICY: return "privacyPolicy";
            case REFUND_POLICY: return "refundPolicy";
            default: return null;
        }
    }
}
