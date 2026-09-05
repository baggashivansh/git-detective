import assert from "node:assert/strict";
import { describe, it } from "node:test";
import {
  canStartIncident,
  incidentStepIndex,
  isIncidentInProgress,
  loadIncidentOrContinue,
  shouldContinueIncident,
} from "./incident-status.ts";

describe("incident flow helpers", () => {
  it("requires both repository and question", () => {
    assert.equal(canStartIncident("", "Why did auth change?"), false);
    assert.equal(canStartIncident("https://github.com/acme/demo", "   "), false);
    assert.equal(
      canStartIncident(
        "https://github.com/acme/demo",
        "Why did authentication become risky after recent changes?",
      ),
      true,
    );
  });

  it("maps phases to workflow steps", () => {
    assert.equal(incidentStepIndex("ANALYZING"), 0);
    assert.equal(incidentStepIndex("INVESTIGATING"), 2);
    assert.equal(incidentStepIndex("VALIDATING"), 3);
    assert.equal(incidentStepIndex("COMPLETED"), 4);
  });

  it("continues while analysis or a queued investigation is waiting", () => {
    assert.equal(
      shouldContinueIncident({
        phase: "ANALYZING",
        status: "QUEUED",
        report: null,
      }),
      true,
    );
    assert.equal(
      shouldContinueIncident({
        phase: "COMPLETED",
        status: "COMPLETED",
        report: { finding: "ok" },
      }),
      false,
    );
    assert.equal(
      shouldContinueIncident({
        phase: "VALIDATING",
        status: "COMPLETED",
        report: null,
      }),
      true,
    );
    assert.equal(
      shouldContinueIncident({
        phase: "INVESTIGATING",
        status: "RUNNING",
        report: null,
      }),
      false,
    );
  });

  it("keeps the last known incident when continue fails", async () => {
    const current = {
      phase: "ANALYZING" as const,
      status: "QUEUED",
      report: null,
    };
    const result = await loadIncidentOrContinue(current, async () => {
      throw new Error("INVESTIGATION_NOT_READY");
    });
    assert.equal(result, current);
  });

  it("treats analyzing and investigating as in-progress", () => {
    assert.equal(isIncidentInProgress("ANALYZING"), true);
    assert.equal(isIncidentInProgress("COMPLETED"), false);
    assert.equal(isIncidentInProgress("FAILED"), false);
  });
});
