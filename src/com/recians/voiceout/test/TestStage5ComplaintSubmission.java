package com.recians.voiceout.test;

import com.recians.voiceout.dao.ComplaintDAO;
import com.recians.voiceout.dao.StudentDAO;
import com.recians.voiceout.model.Category;
import com.recians.voiceout.model.Complaint;
import com.recians.voiceout.model.Student;
import java.util.List;

/**
 * TestStage5ComplaintSubmission - Automated verification of Stage 4 & Stage 5:
 * 1. College email validation & student registration.
 * 2. Non-college email rejection.
 * 3. Student authentication & profile setup.
 * 4. 8 Categories availability.
 * 5. Ordinary complaint submission & unique ID generation (REC-YYYY-XXXXX).
 * 6. Private & Confidential complaint submission.
 * 7. Verification that confidential complaints are excluded from public_complaints_view.
 */
public class TestStage5ComplaintSubmission {

    public static void main(String[] args) {
        System.out.println("================================================================");
        System.out.println("  RECian's VoiceOut - Stage 4 & Stage 5 Live Integration Tests");
        System.out.println("================================================================");

        StudentDAO studentDAO = new StudentDAO();
        ComplaintDAO complaintDAO = new ComplaintDAO();

        String testEmail = "qa.test2026@rajalakshmi.edu.in";
        String testPassword = "TestPassword123!";
        String testName = "REC QA Test Student";

        // TEST 1: Rejection of non-college email
        System.out.println("\n[TEST 1] Testing non-college email rejection...");
        boolean nonCollegeAccepted = studentDAO.isValidCollegeEmail("user@gmail.com");
        if (!nonCollegeAccepted) {
            System.out.println("[PASS] user@gmail.com correctly REJECTED by domain validator.");
        } else {
            System.err.println("[FAIL] Non-college email was improperly accepted!");
            System.exit(1);
        }

        // TEST 2: Acceptance of valid college email
        System.out.println("\n[TEST 2] Testing official college email validation...");
        boolean collegeAccepted = studentDAO.isValidCollegeEmail(testEmail);
        if (collegeAccepted) {
            System.out.println("[PASS] " + testEmail + " correctly ACCEPTED.");
        } else {
            System.err.println("[FAIL] Valid college email was rejected!");
            System.exit(1);
        }

        // TEST 3: Student Registration / Account Retrieval
        System.out.println("\n[TEST 3] Creating or fetching test student account...");
        if (!studentDAO.isEmailRegistered(testEmail)) {
            boolean registered = studentDAO.register(testName, testEmail, testPassword);
            if (registered) {
                System.out.println("[PASS] Test student registered successfully.");
            } else {
                System.err.println("[FAIL] Could not register test student!");
                System.exit(1);
            }
        } else {
            System.out.println("[INFO] Test student account already exists from previous test run.");
        }

        // TEST 4: Student Login Authentication
        System.out.println("\n[TEST 4] Authenticating student credentials...");
        Student authenticatedStudent = studentDAO.login(testEmail, testPassword);
        if (authenticatedStudent != null && authenticatedStudent.getEmail().equalsIgnoreCase(testEmail)) {
            System.out.println("[PASS] Authentication succeeded for: " + authenticatedStudent.getFullName() + " (ID: " + authenticatedStudent.getStudentId() + ")");
        } else {
            System.err.println("[FAIL] Login failed for test credentials!");
            System.exit(1);
        }

        // TEST 5: Profile Completion
        System.out.println("\n[TEST 5] Updating academic profile...");
        boolean profileOk = studentDAO.updateProfile(authenticatedStudent.getStudentId(), "REC-2026-QA01", "Computer Science and Engineering (CSE)", "3rd Year");
        if (profileOk) {
            System.out.println("[PASS] Profile updated: Reg REC-2026-QA01, CSE, 3rd Year.");
        } else {
            System.err.println("[FAIL] Profile update failed!");
            System.exit(1);
        }

        // TEST 6: Verify 8 Categories
        System.out.println("\n[TEST 6] Verifying the 8 required complaint categories...");
        List<Category> categories = complaintDAO.getAllCategories();
        if (categories.size() == 8) {
            System.out.println("[PASS] Exactly 8 categories loaded from database:");
            for (Category c : categories) {
                System.out.println("   - ID " + c.getCategoryId() + ": " + c.getCategoryName() + (c.isConfidential() ? " [CONFIDENTIAL]" : ""));
            }
        } else {
            System.err.println("[FAIL] Expected 8 categories, found: " + categories.size());
            System.exit(1);
        }

        // TEST 7: Submit Ordinary Complaint (Hostel Facilities - ID 1)
        System.out.println("\n[TEST 7] Submitting an Ordinary Complaint (Hostel Facilities)...");
        String ordinaryTitle = "Hostel Block B Hot Water Geyser Maintenance";
        String ordinaryDesc = "The solar water heating system on the second floor of Hostel Block B requires servicing. Water is cold during morning hours.";
        String ordinaryCode = complaintDAO.submitComplaint(authenticatedStudent.getStudentId(), 1, ordinaryTitle, ordinaryDesc);

        if (ordinaryCode != null && ordinaryCode.startsWith("REC-")) {
            System.out.println("[PASS] Ordinary complaint registered with Unique Code: " + ordinaryCode);
        } else {
            System.err.println("[FAIL] Failed to submit ordinary complaint!");
            System.exit(1);
        }

        // TEST 8: Submit Private & Confidential Complaint (Private & Confidential - ID 8)
        System.out.println("\n[TEST 8] Submitting a Private & Confidential Complaint...");
        String confidentialTitle = "Confidential Student Welfare and Safety Concern";
        String confidentialDesc = "Report regarding unauthorized individuals gathering near the rear bus parking after 6 PM. Requires security patrol.";
        String confidentialCode = complaintDAO.submitComplaint(authenticatedStudent.getStudentId(), 8, confidentialTitle, confidentialDesc);

        if (confidentialCode != null && confidentialCode.startsWith("REC-")) {
            System.out.println("[PASS] Confidential complaint registered with Unique Code: " + confidentialCode);
        } else {
            System.err.println("[FAIL] Failed to submit confidential complaint!");
            System.exit(1);
        }

        // TEST 9: Verify Privacy & Public Exclusion
        System.out.println("\n[TEST 9] Verifying that Confidential Complaints are NEVER exposed in public lists...");
        List<Complaint> publicList = complaintDAO.getPublicComplaints();
        boolean ordinaryFoundInPublic = false;
        boolean confidentialFoundInPublic = false;

        for (Complaint pub : publicList) {
            if (pub.getComplaintCode().equals(ordinaryCode)) {
                ordinaryFoundInPublic = true;
            }
            if (pub.getComplaintCode().equals(confidentialCode)) {
                confidentialFoundInPublic = true;
            }
        }

        if (ordinaryFoundInPublic) {
            System.out.println("[PASS] Ordinary complaint (" + ordinaryCode + ") is visible in public view.");
        } else {
            System.err.println("[FAIL] Ordinary complaint was not found in public view!");
            System.exit(1);
        }

        if (!confidentialFoundInPublic) {
            System.out.println("[PASS] Confidential complaint (" + confidentialCode + ") is STRICTLY EXCLUDED from public view!");
        } else {
            System.err.println("[CRITICAL SECURITY FAILED] Confidential complaint leaked into public view!");
            System.exit(1);
        }

        // TEST 10: Verify Complainant can see both in personal complaints
        System.out.println("\n[TEST 10] Verifying complainant's personal grievance list...");
        List<Complaint> myComplaints = complaintDAO.getComplaintsByStudent(authenticatedStudent.getStudentId());
        boolean myOrdinaryFound = false;
        boolean myConfidentialFound = false;

        for (Complaint mc : myComplaints) {
            if (mc.getComplaintCode().equals(ordinaryCode)) myOrdinaryFound = true;
            if (mc.getComplaintCode().equals(confidentialCode) && mc.isConfidential()) myConfidentialFound = true;
        }

        if (myOrdinaryFound && myConfidentialFound) {
            System.out.println("[PASS] Complainant can review both ordinary and confidential issues on personal dashboard.");
        } else {
            System.err.println("[FAIL] Personal complaints list did not contain both filed issues!");
            System.exit(1);
        }

        System.out.println("\n================================================================");
        System.out.println("  ALL 10 TESTS PASSED! Stage 4 and Stage 5 are 100% OPERATIONAL");
        System.out.println("================================================================");
    }
}
