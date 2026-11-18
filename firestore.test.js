const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const FARMER_A = "farmer_alice_1";
const FARMER_B = "farmer_bob_2";
const SUPER_ADMIN_UID = "PMXvtEnXBacRZDKoVBWUk1XFsmw1";

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

test("Unauthenticated user cannot read/write user collections or admin config", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(FARMER_A).get());
  await assertFails(unauthDb.collection("users").doc(FARMER_A).collection("fertilizer_purchases").get());
  await assertFails(
    unauthDb.collection("app_config").doc("settings").set({
      appTitle: "Hacked",
      welcomeBanner: "X",
      irrigationIntervalDays: 4,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  // Public non-PII read on app_config and feeding_recipes succeeds
  await assertSucceeds(unauthDb.collection("app_config").doc("settings").get());
  await assertSucceeds(unauthDb.collection("feeding_recipes").get());
});

test("Super Admin can manage app_config, feeding_recipes, and list users; regular farmer cannot", async () => {
  const adminDb = testEnv.authenticatedContext(SUPER_ADMIN_UID).firestore();
  const farmerDb = testEnv.authenticatedContext(FARMER_A).firestore();
  const now = new Date(Date.now() - 1000);

  await assertSucceeds(
    adminDb.collection("app_config").doc("settings").set({
      appTitle: "স্ট্রবেরি চাষি বন্ধু",
      welcomeBanner: "স্বাগতম",
      dashboardSubtitle: "বাগানের খাতা",
      irrigationIntervalDays: 4,
      workTypes: ["নিড়ানি দেওয়া"],
      fertilizerPresets: ["ইউরিয়া"],
      sprayPresets: ["ম্যানকোজেব"],
      updatedAt: now,
    })
  );

  await assertFails(
    farmerDb.collection("app_config").doc("settings").set({
      appTitle: "Farmer Hack",
      welcomeBanner: "X",
      irrigationIntervalDays: 4,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    adminDb.collection("feeding_recipes").doc("recipe_1").set({
      title: "পকেট গঞ্জ",
      category: "শুকনো সার",
      summary: "বিবরণ",
      dosage: "৫ গ্রাম",
      steps: ["ধাপ ১"],
      tip: "টিপস",
      orderIndex: 1,
      updatedAt: now,
    })
  );

  await assertFails(
    farmerDb.collection("feeding_recipes").doc("recipe_1").delete()
  );

  await assertSucceeds(adminDb.collection("users").get());
  await assertFails(farmerDb.collection("users").get());
});

test("User data isolation and Kotlin payload validation for fertilizer, irrigation, spray, labor, harvest", async () => {
  const aliceDb = testEnv.authenticatedContext(FARMER_A).firestore();
  const bobDb = testEnv.authenticatedContext(FARMER_B).firestore();
  const now = new Date(Date.now() - 1000);

  // Farmer A creates fertilizer purchase (no 'id' field in doc body)
  await assertSucceeds(
    aliceDb.collection("users").doc(FARMER_A).collection("fertilizer_purchases").doc("fert_1").set({
      userId: FARMER_A,
      dateMillis: Date.now(),
      dateLabel: "১৮/১১/২০২৬",
      fertilizerName: "ইউরিয়া",
      shopName: "করিম ট্রেডার্স",
      quantity: "৫ কেজি",
      totalPrice: 450,
      due: true,
      notes: "",
      trashed: false,
      trashedAtMillis: 0,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Partial update to mark Paid (due = false)
  await assertSucceeds(
    aliceDb.collection("users").doc(FARMER_A).collection("fertilizer_purchases").doc("fert_1").update({
      due: false,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Farmer B cannot read or modify Farmer A's records
  await assertFails(bobDb.collection("users").doc(FARMER_A).collection("fertilizer_purchases").get());
  await assertFails(
    bobDb.collection("users").doc(FARMER_A).collection("fertilizer_purchases").doc("fert_1").update({
      due: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Ghost field injection rejected
  await assertFails(
    aliceDb.collection("users").doc(FARMER_A).collection("labor_logs").doc("lab_1").set({
      userId: FARMER_A,
      dateMillis: Date.now(),
      dateLabel: "১৮/১১/২০২৬",
      workerCount: 2,
      dailyWage: 500,
      totalCost: 1000,
      workType: "নিড়ানি দেওয়া",
      notes: "",
      trashed: false,
      trashedAtMillis: 0,
      isAdmin: true,
      createdAt: now,
      updatedAt: now,
    })
  );
});
