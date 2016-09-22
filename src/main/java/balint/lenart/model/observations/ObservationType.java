package balint.lenart.model.observations;

import balint.lenart.model.helper.NamedEnum;

public enum ObservationType implements NamedEnum {

    BLOOD_GLUCOSE_RECORD("Vércukorszint mérés", "hu.uni_pannon.mhealth.dsapi.datatype.BloodGlucoseRecord"),
    BLOOD_PRESSURE_RECORD("Vérnyomás mérés", "hu.uni_pannon.mhealth.dsapi.datatype.BloodPressureRecord"),
    CHGI_LOG_RECORD("Étkezésekhez kapcsolódó GI értékek és szénhidrátfogyasztás", "hu.uni_pannon.mhealth.dsapi.datatype.CHGILogRecord"),
    COMMENT_RECORD("Megjegyzés", "hu.uni_pannon.mhealth.dsapi.datatype.CommentRecord"),
    DIETLOG_ANAM_RECORD("Dietetikai anamnézis", "hu.uni_pannon.mhealth.dsapi.datatype.DietlogAnamRecord"),
    LAB_RECORD("Anamnézist kiegészítő laborértékek", "hu.uni_pannon.mhealth.dsapi.datatype.LabRecord"),
    MEAL_LOG_RECORD("Étkezés napló", "hu.uni_pannon.mhealth.dsapi.datatype.MealLogRecord"),
    MEDICATION_RECORD("Gyógyszerezés napló", "hu.uni_pannon.mhealth.dsapi.datatype.MedicationRecord"),
    NOTIFICATION_RECORD("Beküldött hiányzó élelmiszer adatbázisba történő felvitelről értesítés", "hu.uni_pannon.mhealth.dsapi.datatype.NotificationRecord"),
    PA_LOG_RECORD("Fizikai aktivitási napló", "hu.uni_pannon.mhealth.dsapi.datatype.PALogRecord"),
    WEIGHT_RECORD("Testsúlymérés", "hu.uni_pannon.mhealth.dsapi.datatype.WeightRecord")
    ;

    private final String name;
    private final String className;

    ObservationType(final String name, final String className) {
        this.name = name;
        this.className = className;
    }

    public String getName() {
        return name;
    }

    public String getClassName() {
        return className;
    }
}
