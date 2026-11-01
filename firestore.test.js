const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "gen-lang-client-0970085058";
const ADMIN_UID = "admin_uid_1";
const ADMIN_EMAIL = "serkankocak551@gmail.com";
const ALICE_UID = "alice_student_1";
const ALICE_EMAIL = "alice@student.com";
const BOB_UID = "bob_student_2";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read users", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user: can read own user profile", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).set({
      id: ALICE_UID,
      name: "Alice",
      email: ALICE_EMAIL,
      role: "STUDENT",
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: ALICE_EMAIL }).firestore();
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user: can create own user profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: ALICE_EMAIL }).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      id: ALICE_UID,
      name: "Alice",
      email: ALICE_EMAIL,
      role: "STUDENT",
      packageId: "student_free",
      currentXp: 0,
      lifetimeXp: 0,
      streak: 0,
      studentCapacity: 0,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: cannot read another user's profile", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).set({
      id: ALICE_UID,
      name: "Alice",
      email: ALICE_EMAIL,
      role: "STUDENT",
      createdAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID, { email: "bob@student.com" }).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
});

test("Admin user (serkankocak551@gmail.com): can read another user's profile", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).set({
      id: ALICE_UID,
      name: "Alice",
      email: ALICE_EMAIL,
      role: "STUDENT",
      createdAt: new Date(),
    });
  });

  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();
  await assertSucceeds(adminDb.collection("users").doc(ALICE_UID).get());
});

test("Spoofed admin with email_verified=false: rejected", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).set({
      id: ALICE_UID,
      name: "Alice",
      email: ALICE_EMAIL,
      role: "STUDENT",
      createdAt: new Date(),
    });
  });

  const spoofDb = testEnv.authenticatedContext("spoof_id", {
    email: ADMIN_EMAIL,
    email_verified: false,
  }).firestore();
  await assertFails(spoofDb.collection("users").doc(ALICE_UID).get());
});

test("Student: can create and read own tasks", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: ALICE_EMAIL }).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("tasks").doc("task_1").set({
      id: "task_1",
      userId: ALICE_UID,
      title: "Matematik Fonksiyonlar",
      subject: "Matematik",
      completedQuestionCount: 0,
      targetQuestionCount: 50,
      isCompleted: false,
      createdAt: new Date(),
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("tasks").doc("task_1").get()
  );
});

test("Cross-user: Bob cannot read Alice's tasks", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("tasks").doc("task_1").set({
      id: "task_1",
      userId: ALICE_UID,
      title: "Matematik Fonksiyonlar",
      subject: "Matematik",
      isCompleted: false,
      createdAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID, { email: "bob@student.com" }).firestore();
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("tasks").doc("task_1").get()
  );
});
