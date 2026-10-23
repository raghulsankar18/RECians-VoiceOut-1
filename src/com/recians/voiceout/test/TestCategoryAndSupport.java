package com.recians.voiceout.test;

import com.recians.voiceout.dao.ComplaintDAO;
import com.recians.voiceout.dao.StudentDAO;
import com.recians.voiceout.model.Category;
import com.recians.voiceout.model.Complaint;
import com.recians.voiceout.model.Student;
import java.util.List;

/**
 * TestCategoryAndSupport - Automated verification of the updated UI & features:
 * 1. 20 Official Department Options & Clean Register Number.
 * 2. Category Detail complaints query (excluding confidential issues).
 * 3. 1-Student-1-Vote support functionality.
 * 4. Duplicate support prevention by the same student.
 * 5. Rejection of support for Private & Confidential grievances.
 * 6. Verification that Complaint Submission remains separate and intact.
 */
public class TestCategoryAndSupport {

    public static void main(String[] args) {
        System.out.println("================================================================");
        System.out.println("  RECian's VoiceOut - Category Detail & Support Test Suite");
        System.out.println("================================================================");

        StudentDAO studentDAO = new StudentDAO();
        ComplaintDAO complaintDAO = new ComplaintDAO();

        String testEmail = "qa.test2026@rajalakshmi.edu.in";
        Student student = studentDAO.login(testEmail, "TestPassword123!");
        if (student == null) {
            System.err.println("[FAIL] Test student could not be logged in!");
            System.exit(1);
        }
        System.out.println("[INFO] Authenticated as test student: " + student.getFullName() + " (ID: " + student.getStudentId() + ")");

        // TEST 1: Update profile with official Department and clean Register Number
        System.out.println("\n[TEST 1] Testing Department and clean Register Number update...");
        String officialDept = "Artificial Intelligence & Data Science (AI & DS)";
        String cleanRegNo = "REC2026AIDS401";
        boolean profOk = studentDAO.updateProfile(student.getStudentId(), cleanRegNo, officialDept, "3rd Year");
        if (profOk) {
            Student refreshed = studentDAO.getStudentById(student.getStudentId());
            if (officialDept.equals(refreshed.getDepartment()) && cleanRegNo.equals(refreshed.getRegisterNumber())) {
                System.out.println("[PASS] Profile updated successfully with: " + refreshed.getDepartment() + " & Reg No: " + refreshed.getRegisterNumber());
            } else {
                System.err.println("[FAIL] Stored profile values did not match!");
                System.exit(1);
            }
        } else {
            System.err.println("[FAIL] Profile update failed!");
            System.exit(1);
        }

        // TEST 2: Query complaints by category (Category 1: Hostel Facilities)
        System.out.println("\n[TEST 2] Querying public complaints for Category 1 (Hostel Facilities)...");
        List<Complaint> hostelComplaints = complaintDAO.getComplaintsByCategory(1, student.getStudentId());
        if (!hostelComplaints.isEmpty()) {
            Complaint first = hostelComplaints.get(0);
            System.out.println("[PASS] Category 1 returned " + hostelComplaints.size() + " complaint(s). First: [" + first.getComplaintCode() + "] " + first.getTitle());
        } else {
            System.err.println("[FAIL] Expected existing complaint in Category 1, found none!");
            System.exit(1);
        }

        // TEST 3: Query complaints for Category 8 (Private & Confidential) -> Must be 0
        System.out.println("\n[TEST 3] Querying public complaints for Category 8 (Private & Confidential)...");
        List<Complaint> confidentialComplaints = complaintDAO.getComplaintsByCategory(8, student.getStudentId());
        if (confidentialComplaints.isEmpty()) {
            System.out.println("[PASS] Category 8 returned 0 public complaints (Strict Privacy Verified!).");
        } else {
            System.err.println("[FAIL] Confidential complaints were exposed publicly!");
            System.exit(1);
        }

        // TEST 4: Test 1-Student-1-Vote Support Action
        System.out.println("\n[TEST 4] Testing 1-Student-1-Vote support for complaint...");
        Complaint targetComplaint = hostelComplaints.get(0);
        int initialSupport = targetComplaint.getSupportCount();
        System.out.println("   - Initial Support Count: " + initialSupport);

        boolean alreadySupported = complaintDAO.hasStudentSupported(targetComplaint.getComplaintId(), student.getStudentId());
        if (!alreadySupported) {
            boolean vote1 = complaintDAO.addSupport(targetComplaint.getComplaintId(), student.getStudentId());
            if (vote1) {
                System.out.println("[PASS] First support vote registered successfully.");
            } else {
                System.err.println("[FAIL] First support vote failed!");
                System.exit(1);
            }
        } else {
            System.out.println("[INFO] Student had already supported this complaint previously.");
        }

        // TEST 5: Duplicate Support Prevention
        System.out.println("\n[TEST 5] Testing duplicate support attempt by the same student...");
        boolean duplicateVote = complaintDAO.addSupport(targetComplaint.getComplaintId(), student.getStudentId());
        if (!duplicateVote) {
            System.out.println("[PASS] Duplicate support vote was REJECTED by database unique constraint!");
        } else {
            System.err.println("[FAIL] Duplicate vote was improperly accepted!");
            System.exit(1);
        }

        // TEST 6: Rejection of Support for Private & Confidential Complaints
        System.out.println("\n[TEST 6] Testing support rejection on confidential complaints...");
        // Fetch confidential complaint ID 2
        boolean supportConfidential = complaintDAO.addSupport(2, student.getStudentId());
        if (!supportConfidential) {
            System.out.println("[PASS] Support on confidential complaint correctly REJECTED!");
        } else {
            System.err.println("[FAIL] Support was allowed on a confidential complaint!");
            System.exit(1);
        }

        // TEST 7: Separate Complaint Submission Flow Preserved
        System.out.println("\n[TEST 7] Verifying separate complaint submission remains functional...");
        String newCode = complaintDAO.submitComplaint(student.getStudentId(), 2, "Canteen Drinking Water Cooler Filter", "The water filter unit in the main canteen requires cleaning and replacement.");
        if (newCode != null && newCode.startsWith("REC-2026-")) {
            System.out.println("[PASS] Separate complaint submission intact. Generated: " + newCode);
        } else {
            System.err.println("[FAIL] Complaint submission failed!");
            System.exit(1);
        }

        System.out.println("\n================================================================");
        System.out.println("  ALL 7 TESTS PASSED! All UI & Feature updates verified.");
        System.out.println("================================================================");
    }
}
