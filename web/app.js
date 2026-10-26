// Frontend for the Smart Canteen Billing System.
// The Java backend gives the menu, saves orders and returns the final bill.

const MAX_QTY = 20;
let menu = [];
const cart = new Map(); // item id -> quantity
let category = "All";

const rupees = (n) => "₹" + Number(n).toFixed(2);

// ---------- helpers ----------

async function api(path, form) {
  const options = form ? { method: "POST", body: new URLSearchParams(form) } : {};
  const res = await fetch(path, options);
  const data = await res.json();
  if (!res.ok) throw new Error(data.error || "Something went wrong");
  return data;
}

function toast(message, isError) {
  const t = document.getElementById("toast");
  t.textContent = message;
  t.className = "toast" + (isError ? " error" : "");
  t.hidden = false;
  clearTimeout(toast.timer);
  toast.timer = setTimeout(() => (t.hidden = true), 3000);
}

function el(tag, className, text) {
  const e = document.createElement(tag);
  if (className) e.className = className;
  if (text !== undefined) e.textContent = text;
  return e;
}

// ---------- tabs ----------

document.querySelectorAll(".tabs button").forEach((btn) =>
  btn.addEventListener("click", () => {
    document.querySelectorAll(".tabs button").forEach((b) => b.classList.toggle("active", b === btn));
    document.querySelectorAll(".tab-panel").forEach((p) => (p.hidden = p.id !== btn.dataset.tab));
    if (btn.dataset.tab === "report") loadReport();
  })
);

document.querySelectorAll(".chip").forEach((chip) =>
  chip.addEventListener("click", () => {
    category = chip.dataset.cat;
    document.querySelectorAll(".chip").forEach((c) => c.classList.toggle("active", c === chip));
    renderMenu();
  })
);

// ---------- menu ----------

function renderMenu() {
  const grid = document.getElementById("menuGrid");
  grid.innerHTML = "";
  menu
    .filter((m) => category === "All" || m.category === category)
    .forEach((m) => {
      const qty = cart.get(m.id) || 0;
      const card = el("div", "item" + (qty > 0 ? " in-cart" : ""));
      card.append(
        el("span", "tag " + m.category, m.category + " · " + Math.round(m.gstRate * 100) + "% GST"),
        el("h3", "", m.name),
        el("div", "price", rupees(m.price))
      );
      const stepper = el("div", "stepper");
      const minus = el("button", "", "−");
      minus.type = "button";
      minus.setAttribute("aria-label", "Remove one " + m.name);
      minus.disabled = qty === 0;
      minus.addEventListener("click", () => changeQty(m.id, -1));
      const plus = el("button", "", "+");
      plus.type = "button";
      plus.setAttribute("aria-label", "Add one " + m.name);
      plus.addEventListener("click", () => changeQty(m.id, 1));
      stepper.append(minus, el("span", "", String(qty)), plus);
      card.appendChild(stepper);
      grid.appendChild(card);
    });
}

function changeQty(id, delta) {
  const next = (cart.get(id) || 0) + delta;
  if (next > MAX_QTY) {
    toast("Quantity must be between 1 and " + MAX_QTY + " per item", true);
    return;
  }
  if (next <= 0) cart.delete(id);
  else cart.set(id, next);
  renderMenu();
  renderCart();
}

// ---------- cart (live preview; the server makes the final bill) ----------

function renderCart() {
  const box = document.getElementById("cartLines");
  box.innerHTML = "";
  let sub = 0;
  let gst = 0;
  cart.forEach((qty, id) => {
    const m = menu.find((x) => x.id === id);
    const amount = m.price * qty;
    sub += amount;
    gst += amount * m.gstRate;
    const line = el("div", "cart-line");
    const left = el("span", "", m.name + " × " + qty + " ");
    left.appendChild(el("small", "", "@ " + rupees(m.price)));
    line.append(left, el("span", "", rupees(amount)));
    box.appendChild(line);
  });
  if (cart.size === 0) box.appendChild(el("p", "empty", "No items yet. Tap + on any item."));
  document.getElementById("cSub").textContent = rupees(sub);
  document.getElementById("cGst").textContent = rupees(gst);
  document.getElementById("cTotal").textContent = rupees(sub + gst);
  document.getElementById("placeBtn").disabled = cart.size === 0;
  return sub + gst;
}

document.getElementById("clearBtn").addEventListener("click", () => {
  cart.clear();
  renderMenu();
  renderCart();
});

document.getElementById("placeBtn").addEventListener("click", async () => {
  const total = renderCart();
  if (!confirm("Confirm order? Total " + rupees(total))) return;
  const items = Array.from(cart, ([id, qty]) => id + ":" + qty).join(",");
  try {
    const bill = await api("/api/order", { items });
    showBill(bill);
    cart.clear();
    renderMenu();
    renderCart();
  } catch (e) {
    toast(e.message, true);
  }
});

// ---------- bill ----------

function showBill(b) {
  document.getElementById("billToken").textContent = b.token;
  document.getElementById("billTime").textContent = b.time;
  const body = document.getElementById("billBody");
  body.innerHTML = "";
  b.items.forEach((it) => {
    const tr = document.createElement("tr");
    [it.name, it.qty, Number(it.price).toFixed(2), Number(it.amount).toFixed(2)].forEach((v) => tr.appendChild(el("td", "", String(v))));
    body.appendChild(tr);
  });
  document.getElementById("bSub").textContent = rupees(b.subtotal);
  document.getElementById("bGst").textContent = rupees(b.gst);
  document.getElementById("bTotal").textContent = rupees(b.total);
  document.getElementById("billBack").hidden = false;
  document.getElementById("billClose").focus();
}

document.getElementById("billClose").addEventListener("click", () => (document.getElementById("billBack").hidden = true));
document.addEventListener("keydown", (e) => {
  if (e.key === "Escape") document.getElementById("billBack").hidden = true;
});

// ---------- sales report ----------

async function loadReport() {
  try {
    const r = await api("/api/report");
    document.getElementById("rOrders").textContent = r.totalOrders;
    document.getElementById("rRevenue").textContent = rupees(r.revenue);
    document.getElementById("rBest").textContent = r.bestSeller || "–";
    document.getElementById("rBestQty").textContent = r.bestSeller ? "Best seller (" + r.bestQty + " sold)" : "Best seller";
    const bars = document.getElementById("reportBars");
    bars.innerHTML = "";
    if (r.items.length === 0) {
      bars.appendChild(el("p", "empty", "No sales yet. Place an order first."));
      return;
    }
    const max = r.items[0].qty;
    r.items.forEach((it) => {
      const row = el("div", "bar-row");
      const track = el("div", "bar-track");
      const fill = el("div", "bar-fill");
      fill.style.width = (it.qty / max) * 100 + "%";
      track.appendChild(fill);
      row.append(el("span", "", it.name), track, el("span", "val", it.qty + " sold · " + rupees(it.amount)));
      bars.appendChild(row);
    });
  } catch (e) {
    toast(e.message, true);
  }
}

// ---------- start ----------

(async function start() {
  try {
    menu = await api("/api/menu");
    renderMenu();
    renderCart();
  } catch (e) {
    toast("Could not load the menu. Is the Java server running?", true);
  }
})();
