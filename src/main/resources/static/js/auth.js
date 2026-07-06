/* IndusTrust Bank — login / register page logic */
const $ = (s, r = document) => r.querySelector(s);
const $$ = (s, r = document) => [...r.querySelectorAll(s)];

const form = $("#authForm");
let mode = "login";

function toast(msg, type = "ok") {
  const t = document.createElement("div");
  t.className = "toast" + (type === "error" ? " toast--error" : "");
  t.innerHTML = `<span class="toast__ic">${type === "error" ? "!" : "\u2713"}</span>${escapeHtml(msg)}`;
  $("#toasts").appendChild(t);
  setTimeout(() => { t.style.opacity = "0"; t.style.transform = "translateX(16px)"; t.style.transition = "all .2s"; setTimeout(() => t.remove(), 200); }, 3000);
}
const escapeHtml = (v) => v == null ? "" : String(v).replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));

function setMode(m) {
  mode = m;
  $$(".auth-tab").forEach((t) => t.setAttribute("aria-selected", String(t.dataset.mode === m)));
  const register = m === "register";
  $("#nameField").hidden = !register;
  $("#authHeading").textContent = register ? "Create your account" : "Welcome back";
  $("#authSubtext").textContent = register ? "Open a free account in seconds." : "Sign in to manage your accounts.";
  $("#authSubmit").textContent = register ? "Create account" : "Sign in";
  form.elements.password.setAttribute("autocomplete", register ? "new-password" : "current-password");
  clearErrors();
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
}

$$(".auth-tab").forEach((t) => t.addEventListener("click", () => setMode(t.dataset.mode)));

form.addEventListener("submit", async (e) => {
  e.preventDefault();
  clearErrors();
  const data = Object.fromEntries(new FormData(form).entries());
  const btn = $("#authSubmit");
  btn.disabled = true;
  try {
    if (mode === "register") {
      const res = await fetch("/api/auth/register", {
        method: "POST", headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ fullName: data.fullName, email: data.email, password: data.password }),
      });
      if (res.status === 400) { showErrors((await res.json()).fieldErrors || {}); return; }
      if (res.status === 409) { toast("An account already exists for that email", "error"); return; }
      if (!res.ok) throw new Error();
    }
    // login (also runs right after a successful register)
    const login = await fetch("/api/auth/login", {
      method: "POST", headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email: data.email, password: data.password }),
    });
    if (login.status === 401) { toast("Invalid email or password", "error"); return; }
    if (login.status === 400) { showErrors((await login.json()).fieldErrors || {}); return; }
    if (!login.ok) throw new Error();
    window.location.href = "/";
  } catch {
    toast("Something went wrong — please try again", "error");
  } finally {
    btn.disabled = false;
  }
});

// If already signed in, skip the login page.
fetch("/api/auth/me").then((r) => { if (r.ok) window.location.href = "/"; }).catch(() => {});
