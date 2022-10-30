package com.supercom.puretrack.model.business_logic_models.knox;

import com.supercom.puretrack.util.general.KnoxUtil;

public class OffenderKnoxModeFactory {

    public static BaseKnoxMode getOffenderMode(KnoxUtil.OffenderModeTypes currentOffenderModeType) {

        switch (currentOffenderModeType) {

            case Officer_Mode:
                return new OfficerKnoxMode();

            case Offender_Normal_Mode:
                return new OffenderNormalKnoxMode();

            case Offender_Flight_Mode:
                return new OffenderFlightKnoxMode();


        }

        return null;
    }
}
