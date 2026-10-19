import { readFileSync } from 'node:fs';
import {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment,
  type RulesTestEnvironment,
} from '@firebase/rules-unit-testing';
import { deleteDoc, doc, getDoc, serverTimestamp, setDoc } from 'firebase/firestore';
import { afterAll, beforeAll, beforeEach, describe, it } from 'vitest';

let env: RulesTestEnvironment;

beforeAll(async () => {
  env = await initializeTestEnvironment({
    projectId: 'demo-flux',
    firestore: { rules: readFileSync('firestore.rules', 'utf8'), host: '127.0.0.1', port: 8080 },
  });
});

beforeEach(() => env.clearFirestore());

afterAll(() => env.cleanup());

const validUser = {
  displayName: 'Daniela',
  link: { phoneInstallationId: 'phone-1', watchNodeId: 'node-1', linkedAt: 1_760_000_000_000 },
  updatedAt: serverTimestamp(),
};

describe('users/{uid}', () => {
  it('lets a user write and read their own document', async () => {
    const db = env.authenticatedContext('alice').firestore();
    await assertSucceeds(setDoc(doc(db, 'users/alice'), validUser));
    await assertSucceeds(getDoc(doc(db, 'users/alice')));
  });

  it('blocks reading or writing another user', async () => {
    await env.withSecurityRulesDisabled((ctx) => setDoc(doc(ctx.firestore(), 'users/bob'), { displayName: 'Bob' }));
    const db = env.authenticatedContext('alice').firestore();
    await assertFails(getDoc(doc(db, 'users/bob')));
    await assertFails(setDoc(doc(db, 'users/bob'), validUser));
  });

  it('blocks unauthenticated access', async () => {
    const db = env.unauthenticatedContext().firestore();
    await assertFails(getDoc(doc(db, 'users/alice')));
    await assertFails(setDoc(doc(db, 'users/alice'), validUser));
  });

  it('validates fields, types and sizes', async () => {
    const db = env.authenticatedContext('alice').firestore();
    await assertFails(setDoc(doc(db, 'users/alice'), { ...validUser, role: 'admin' }));
    await assertFails(setDoc(doc(db, 'users/alice'), { ...validUser, displayName: 'x'.repeat(41) }));
    await assertFails(setDoc(doc(db, 'users/alice'), { ...validUser, link: { phoneInstallationId: 42 } }));
    await assertFails(setDoc(doc(db, 'users/alice'), { ...validUser, link: { ...validUser.link, extra: true } }));
    await assertSucceeds(setDoc(doc(db, 'users/alice'), { link: { phoneInstallationId: 'p', watchNodeId: null } }));
  });

  it('does not allow deleting the account document', async () => {
    const db = env.authenticatedContext('alice').firestore();
    await assertSucceeds(setDoc(doc(db, 'users/alice'), validUser));
    await assertFails(deleteDoc(doc(db, 'users/alice')));
  });

  it('closes every other collection', async () => {
    const db = env.authenticatedContext('alice').firestore();
    await assertFails(setDoc(doc(db, 'notes/n1'), { text: 'hola' }));
  });
});
