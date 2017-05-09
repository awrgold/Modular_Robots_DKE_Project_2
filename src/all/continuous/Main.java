package all.continuous;

import javafx.geometry.Point3D;

public class Main {
    public final static boolean DEBUG = false;
    public final static boolean VALIDATE_EVERYTHING = false;

    public static void main(String[] args) throws InvalidMoveException, InvalidStateException {
        //TestCases.basicTestCopyingAndData();
        //TestCases.basicTestAutomaticStateValidation();

        TestCases.collisionDetectionTest2();
    }
}
