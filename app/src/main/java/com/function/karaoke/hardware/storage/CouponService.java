package com.function.karaoke.hardware.storage;

import com.function.karaoke.hardware.activities.Model.Coupon;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class manage sign up and getting the user object.
 */
public class CouponService {
    private static final String COLLECTION_COUPONS = "coupons";

    private static final String DOWNLOADS = "shares";
    private static final String VIEWS = "views";
    private static final String TYPE = "subscriptionType";
    private static final String COUPON_CODE = "code";

    private static final int NOT_EXIST = -1;
    private static final int MONTHLY = 1;
    private static final int YEARLY = 2;
    private static final int FREE_SHARES = 3;
    private static final String EMAILS = "emails";

    private DatabaseDriver databaseDriver;
    private AuthenticationDriver authenticationDriver;
    private CollectionReference couponsCollectionRef;
    public static final String Coupons = "coupons";
    public static final String UID = "id";
    private static final String TAG = UserService.class.getSimpleName();
    private Coupon coupon;
    private DocumentReference couponDocument;
    private final List<String> fields = new ArrayList<>();

    public CouponService(DatabaseDriver databaseDriver) {
        this.databaseDriver = databaseDriver;
        couponsCollectionRef = databaseDriver.getCollectionReferenceByName(Coupons);
    }

    public void validateCoupon(GetCouponType getCouponType, String code, String emailAddress) {
        final List<Coupon> documentsList = new LinkedList<>();
        Query getUserQuery = couponsCollectionRef.whereEqualTo(COUPON_CODE, code);
        getUserQuery.get().addOnCompleteListener(task -> {
            if (task.isComplete())
                if (task.isSuccessful()) {
                    if (task.getResult().isEmpty()) {
                        coupon = null;
                        getCouponType.type(NOT_EXIST);
                    } else {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            this.couponDocument = task.getResult().getDocuments().get(0).getReference();
                            documentsList.add(document.toObject(Coupon.class));
                        }
                        coupon = documentsList.get(0);
                        checkIfEmailIsInList(getCouponType, coupon, emailAddress);
                    }
                } else getCouponType.type(NOT_EXIST);
        });
//        return success;
    }

    private void checkIfEmailIsInList(GetCouponType getCouponType, Coupon coupon, String emailAddress) {
        if (coupon.getEmails().toLowerCase().contains(emailAddress.toLowerCase())) {
            removeEmailFromCoupon(coupon, emailAddress);
            if (coupon.getType() == FREE_SHARES)
                getCouponType.freeShares(FREE_SHARES, coupon.getFreeShares());
            else
                getCouponType.type(coupon.getType());
        } else
            getCouponType.type(NOT_EXIST);
    }

    private void removeEmailFromCoupon(Coupon coupon, String emailAddress) {
        String emails = this.coupon.getEmails().toLowerCase().replaceFirst(emailAddress.toLowerCase(), "");
        changeCoupon(emails);

    }

    //
    private void changeCoupon(String emails) {
        Map<String, Object> data = new HashMap<>();
        data.put(EMAILS, emails);
        couponDocument.update(data);
    }

    public interface GetCouponType {
        void type(int type);

        void freeShares(int type, int freeShares);
    }
}
