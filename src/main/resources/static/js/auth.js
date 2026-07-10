/* IndusTrust Bank — login / register page logic */
const $ = (s, r = document) => r.querySelector(s);
const $$ = (s, r = document) => [...r.querySelectorAll(s)];

const form = $("#authForm");
const mfaForm = $("#mfaForm");
let mode = "login";
let regStep = 1;
const REG_STEPS = 4;

function toast(msg, type = "ok") {
  const t = document.createElement("div");
  t.className = "toast" + (type === "error" ? " toast--error" : "");
  t.innerHTML = `<span class="toast__ic">${type === "error" ? "!" : "\u2713"}</span>${escapeHtml(msg)}`;
  $("#toasts").appendChild(t);
  setTimeout(() => { t.style.opacity = "0"; t.style.transform = "translateX(16px)"; t.style.transition = "all .2s"; setTimeout(() => t.remove(), 200); }, 3000);
}
const escapeHtml = (v) => v == null ? "" : String(v).replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));

/* ---------- Mode: sign in (single step) vs create account (wizard) ---------- */
function setMode(m) {
  mode = m;
  $$(".auth-tab").forEach((t) => t.setAttribute("aria-selected", String(t.dataset.mode === m)));
  const register = m === "register";

  $(`[data-mode-panel="login"]`).classList.toggle("is-active", !register);
  $("#regStepper").hidden = !register;
  $("#authDemo").hidden = register;

  if (register) {
    regStep = 1;
    renderRegStep();
  } else {
    $$("[data-reg-step]").forEach((p) => p.classList.remove("is-active"));
  }

  $("#authHeading").textContent = register ? "Create your account" : "Welcome back";
  $("#authSubtext").textContent = register ? "A few quick steps and you're set up." : "Sign in to manage your accounts.";
  clearErrors();
}
$$(".auth-tab").forEach((t) => t.addEventListener("click", () => setMode(t.dataset.mode)));

/* ---------- Signup wizard: step rendering ---------- */
function renderRegStep() {
  $$("[data-reg-step]").forEach((p) => p.classList.toggle("is-active", Number(p.dataset.regStep) === regStep));
  $$("[data-step-indicator]").forEach((el) => {
    const n = Number(el.dataset.stepIndicator);
    el.classList.toggle("is-active", n === regStep);
    el.classList.toggle("is-done", n < regStep);
  });
  $$("[data-step-label]").forEach((el) => el.classList.toggle("is-active", Number(el.dataset.stepLabel) === regStep));
  if (regStep === REG_STEPS) buildReview();
  const active = $(`[data-reg-step="${regStep}"]`);
  const firstInput = active && active.querySelector("input:not([type=checkbox])");
  if (firstInput) setTimeout(() => firstInput.focus(), 60);
}

const REG_STEP_FIELDS = {
  1: ["fullName", "regEmail"],
  2: ["regPassword", "confirmPassword"],
  3: ["phone", "dateOfBirth", "panNumber", "address"],
};

function validateRegStep(step) {
  const errs = {};
  const v = (name) => (form.elements[name] ? form.elements[name].value.trim() : "");

  if (step === 1) {
    if (!v("fullName")) errs.fullName = "Enter your full name";
    const email = v("regEmail");
    if (!email) errs.regEmail = "Enter your email address";
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) errs.regEmail = "Enter a valid email address";
  }
  if (step === 2) {
    const pw = v("regPassword");
    const cpw = v("confirmPassword");
    if (!pw) errs.regPassword = "Create a password";
    else if (pw.length < 8) errs.regPassword = "Use at least 8 characters";
    if (!cpw) errs.confirmPassword = "Re-enter your password";
    else if (pw && cpw !== pw) errs.confirmPassword = "Passwords don't match";
  }
  if (step === 3) {
    if (!/^[6-9][0-9]{9}$/.test(v("phone"))) errs.phone = "Enter a valid 10-digit mobile number";
    if (!v("dateOfBirth")) errs.dateOfBirth = "Enter your date of birth";
    if (!/^[A-Z]{5}[0-9]{4}[A-Z]$/.test(v("panNumber").toUpperCase())) errs.panNumber = "PAN must look like ABCDE1234F";
    if (!v("address")) errs.address = "Enter your residential address";
  }
  return errs;
}

$$("[data-step-next]").forEach((btn) => btn.addEventListener("click", () => {
  const errs = validateRegStep(regStep);
  if (Object.keys(errs).length) { showErrors(errs); return; }
  clearErrors();
  regStep = Math.min(REG_STEPS, regStep + 1);
  renderRegStep();
}));
$$("[data-step-back]").forEach((btn) => btn.addEventListener("click", () => {
  clearErrors();
  regStep = Math.max(1, regStep - 1);
  renderRegStep();
}));

function buildReview() {
  const v = (name) => (form.elements[name] ? form.elements[name].value.trim() : "");
  const rows = [
    ["Full name", v("fullName")],
    ["Email", v("regEmail")],
    ["Mobile number", v("phone")],
    ["Date of birth", v("dateOfBirth")],
    ["PAN number", v("panNumber").toUpperCase()],
    ["Address", v("address")],
  ];
  $("#reviewList").innerHTML = rows.map(([k, val]) =>
    `<div class="review-list__row"><span>${escapeHtml(k)}</span><span>${escapeHtml(val || "—")}</span></div>`
  ).join("");
}

/* ---------- Password strength meter (step 2) ---------- */
const pwInput = form.elements["regPassword"];
function pwStrength(pw) {
  if (!pw) return 0;
  let score = 0;
  if (pw.length >= 8) score++;
  if (pw.length >= 12) score++;
  if (/[a-z]/.test(pw) && /[A-Z]/.test(pw)) score++;
  if (/[0-9]/.test(pw) && /[^A-Za-z0-9]/.test(pw)) score++;
  return Math.max(pw.length >= 8 ? 1 : 0, Math.min(4, score));
}
const STRENGTH_LABELS = ["Enter a password", "Weak — add more characters", "Fair — try mixing case & numbers", "Good — add a symbol for extra strength", "Strong password"];
if (pwInput) {
  pwInput.addEventListener("input", () => {
    const level = pwStrength(pwInput.value);
    $("#pwStrength").dataset.level = String(level);
    $("#pwStrengthLabel").textContent = STRENGTH_LABELS[level];
  });
}

function clearErrors() {
  $$(".field__error", form).forEach((e) => (e.textContent = ""));
  $$("input", form).forEach((e) => e.classList.remove("invalid"));
}
function showErrors(errs) {
  clearErrors();
  for (const [k, v] of Object.entries(errs)) {
    const box = $(`[data-error="${k}"]`, form);
    const input = form.elements[k];
    if (box) box.textContent = v;
    if (input) input.classList.add("invalid");
  }
  // Jump back to whichever register step actually owns the first error, if we're past it.
  if (mode === "register") {
    for (const [step, fields] of Object.entries(REG_STEP_FIELDS)) {
      if (fields.some((f) => errs[f]) && Number(step) < regStep) {
        regStep = Number(step);
        renderRegStep();
        setTimeout(() => showErrors(errs), 30);
        break;
      }
    }
  }
}

/* ---------- Animated password show/hide ---------- */
$$("[data-toggle-pw]").forEach((btn) => {
  btn.addEventListener("click", () => {
    const input = btn.closest(".field--password").querySelector("input");
    const showing = input.type === "text";
    btn.classList.add("is-blinking");
    setTimeout(() => {
      input.type = showing ? "password" : "text";
      btn.setAttribute("aria-pressed", String(!showing));
      btn.setAttribute("aria-label", showing ? "Show password" : "Hide password");
    }, 170);
    setTimeout(() => btn.classList.remove("is-blinking"), 380);
  });
});

/* ---------- Enter key advances the wizard instead of submitting mid-flow ---------- */
form.addEventListener("keydown", (e) => {
  if (e.key !== "Enter" || e.target.tagName !== "INPUT") return;
  if (mode !== "register" || regStep === REG_STEPS) return; // last step: let Enter submit normally
  e.preventDefault();
  const nextBtn = $(`[data-reg-step="${regStep}"] [data-step-next]`);
  if (nextBtn) nextBtn.click();
});

/* ---------- login / register submit ---------- */
form.addEventListener("submit", async (e) => {
  e.preventDefault();
  if (mode === "register") {
    const errs = validateRegStep(3);
    Object.assign(errs, validateRegStep(2), validateRegStep(1));
    if (Object.keys(errs).length) { showErrors(errs); return; }
    if (!$("#consentCheck").checked) { toast("Please accept the Terms & Conditions to continue", "error"); return; }
  }
  clearErrors();
  const data = Object.fromEntries(new FormData(form).entries());
  const submitBtn = mode === "register" ? $("#authSubmit-register") : $("#authSubmit");
  submitBtn.disabled = true;
  try {
    const loginEmail = mode === "register" ? data.regEmail : data.email;
    const loginPassword = mode === "register" ? data.regPassword : data.password;

    if (mode === "register") {
      const res = await fetch("/api/auth/register", {
        method: "POST", headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          fullName: data.fullName, email: data.regEmail, password: data.regPassword,
          phone: data.phone, dateOfBirth: data.dateOfBirth,
          panNumber: (data.panNumber || "").toUpperCase(), address: data.address,
        }),
      });
      if (res.status === 400) { showErrors((await res.json()).fieldErrors || {}); return; }
      if (res.status === 409) { toast("An account already exists for that email", "error"); regStep = 1; renderRegStep(); return; }
      if (!res.ok) throw new Error();
    }
    // login (also runs right after a successful register)
    const login = await fetch("/api/auth/login", {
      method: "POST", headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email: loginEmail, password: loginPassword }),
    });
    if (login.status === 401) { toast("Invalid email or password", "error"); return; }
    if (login.status === 400) { showErrors((await login.json()).fieldErrors || {}); return; }
    if (!login.ok) throw new Error();

    const body = await login.json();
    if (body.mfaRequired) {
      showMfaStep(body.fullName);
      return;
    }
    window.location.href = "/";
  } catch {
    toast("Something went wrong — please try again", "error");
  } finally {
    submitBtn.disabled = false;
  }
});

/* ---------- 2FA step ---------- */
function showMfaStep(fullName) {
  form.hidden = true;
  $("#authDemo").hidden = true;
  $$(".auth-tabs")[0].hidden = true;
  $("#regStepper").hidden = true;
  mfaForm.hidden = false;
  $("#authHeading").textContent = "Verify it's you";
  $("#authSubtext").textContent = fullName ? `Welcome back, ${fullName.split(" ")[0]}.` : "Enter your authentication code.";
  mfaForm.reset();
  $$(".field__error", mfaForm).forEach((e) => (e.textContent = ""));
  setTimeout(() => mfaForm.elements.code.focus(), 60);
}

function backToLogin() {
  mfaForm.hidden = true;
  form.hidden = false;
  $$(".auth-tabs")[0].hidden = false;
  setMode("login");
}
$("#mfaBack").addEventListener("click", backToLogin);

mfaForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  const errBox = $('[data-error="code"]', mfaForm);
  errBox.textContent = "";
  const code = mfaForm.elements.code.value.trim();
  const btn = $("#mfaSubmit");
  btn.disabled = true;
  try {
    const res = await fetch("/api/auth/login/2fa", {
      method: "POST", headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ code }),
    });
    if (res.status === 400) {
      const body = await res.json().catch(() => ({}));
      errBox.textContent = body.fieldErrors?.code || body.message || "Enter a 6-digit code";
      return;
    }
    if (res.status === 403) {
      const body = await res.json().catch(() => ({}));
      errBox.textContent = body.message || "Incorrect code";
      if ((body.message || "").includes("sign in again")) { toast(body.message, "error"); backToLogin(); }
      return;
    }
    if (!res.ok) throw new Error();
    window.location.href = "/";
  } catch {
    toast("Something went wrong — please try again", "error");
  } finally {
    btn.disabled = false;
  }
});

// If already signed in, skip the login page.
fetch("/api/auth/me").then((r) => { if (r.ok) window.location.href = "/"; }).catch(() => {});
