/**
 * CineBook - Movie Ticket Booking System
 * Interactive Frontend Controller (Vanilla JavaScript)
 * Demonstrates OOP models, dynamic 5x8 seat mapping, coupon calculation, and CRUD operations.
 */

// =============================================================================
// 1. Data Store (Matches Java Backend Domain Model)
// =============================================================================

const MOVIES = [
    {
        id: 1,
        title: "Interstellar",
        genre: "Sci-Fi / Adventure",
        duration: "169 mins",
        rating: "8.7",
        director: "Christopher Nolan",
        cast: "Matthew McConaughey, Anne Hathaway, Jessica Chastain",
        synopsis: "A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival as Earth faces ecological collapse.",
        poster: "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?auto=format&fit=crop&w=800&q=80",
        shows: [
            { id: 101, timeText: "10:00 AM", bookedSeats: ["A1", "A2", "B5"] },
            { id: 102, timeText: "7:30 PM", bookedSeats: ["C1", "C2", "E3", "E4"] }
        ]
    },
    {
        id: 2,
        title: "Dune: Part Two",
        genre: "Sci-Fi / Action",
        duration: "166 mins",
        rating: "8.6",
        director: "Denis Villeneuve",
        cast: "Timothée Chalamet, Zendaya, Rebecca Ferguson",
        synopsis: "Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family.",
        poster: "https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=800&q=80",
        shows: [
            { id: 201, timeText: "4:15 PM", bookedSeats: ["B1", "B2", "D4"] }
        ]
    },
    {
        id: 3,
        title: "The Dark Knight",
        genre: "Action / Crime",
        duration: "152 mins",
        rating: "9.0",
        director: "Christopher Nolan",
        cast: "Christian Bale, Heath Ledger, Aaron Eckhart",
        synopsis: "When the menace known as the Joker wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest psychological and physical tests of his ability to fight injustice.",
        poster: "https://images.unsplash.com/photo-1509281373149-e957c6296406?auto=format&fit=crop&w=800&q=80",
        shows: [
            { id: 301, timeText: "9:45 PM", bookedSeats: ["E1", "E2", "C7", "C8"] }
        ]
    }
];

// Seat pricing structure matching SeatType enum in Java
const SEAT_TYPES = {
    A: { type: "Regular", price: 150 },
    B: { type: "Regular", price: 150 },
    C: { type: "Premium", price: 250 },
    D: { type: "Premium", price: 250 },
    E: { type: "VIP",     price: 400 }
};

const ROWS = ['A', 'B', 'C', 'D', 'E'];
const COLS = 8;
const REFUND_RATE = 0.80; // 80% refund on cancellation

// Active session state
let currentMovie = null;
let currentShow = null;
let selectedSeats = [];
let appliedCoupon = null;

// =============================================================================
// 2. Initialization & DOM Rendering
// =============================================================================

document.addEventListener("DOMContentLoaded", () => {
    initDefaultBookings();
    renderMovieCards();
    renderBookingsTable();
    setupEventListeners();
});

function initDefaultBookings() {
    if (!localStorage.getItem("cinebook_web_bookings")) {
        const initialBookings = [
            {
                bookingId: "BK1001",
                movieTitle: "Interstellar",
                showTime: "7:30 PM",
                seatCodes: "C3, C4",
                baseAmount: 500,
                discount: 50,
                amountPaid: 450,
                status: "CONFIRMED",
                bookingTime: "2026-10-30 14:15",
                refundAmount: 0
            }
        ];
        localStorage.setItem("cinebook_web_bookings", JSON.stringify(initialBookings));
    }
}

function getStoredBookings() {
    try {
        return JSON.parse(localStorage.getItem("cinebook_web_bookings")) || [];
    } catch (e) {
        return [];
    }
}

function saveBookings(bookings) {
    localStorage.setItem("cinebook_web_bookings", JSON.stringify(bookings));
    renderBookingsTable();
}

// Render now-showing movies
function renderMovieCards() {
    const container = document.getElementById("moviesGrid");
    if (!container) return;

    container.innerHTML = MOVIES.map(movie => `
        <div class="movie-card">
            <div class="movie-poster-box" style="background-image: url('${movie.poster}')">
                <div class="badge-row">
                    <span class="rating-badge">★ ${movie.rating}</span>
                    <span class="genre-badge">${movie.genre}</span>
                </div>
                <div class="movie-info">
                    <h3 class="movie-title">${movie.title}</h3>
                    <p class="movie-meta">${movie.duration} • Directed by ${movie.director}</p>
                </div>
            </div>
            <div class="movie-body">
                <p class="movie-synopsis">${movie.synopsis}</p>
                <div>
                    <div class="showtimes-label">Select Show Time:</div>
                    <div class="showtime-chips">
                        ${movie.shows.map(show => `
                            <button class="show-chip" onclick="openSeatModal(${movie.id}, ${show.id})">
                                🕒 ${show.timeText}
                            </button>
                        `).join('')}
                    </div>
                </div>
            </div>
        </div>
    `).join('');
}

// =============================================================================
// 3. 5x8 Live Seat Map & Booking Logic
// =============================================================================

window.openSeatModal = function(movieId, showId) {
    currentMovie = MOVIES.find(m => m.id === movieId);
    currentShow = currentMovie.shows.find(s => s.id === showId);
    selectedSeats = [];
    appliedCoupon = null;

    document.getElementById("modalMovieTitle").textContent = `${currentMovie.title} (${currentShow.timeText})`;
    document.getElementById("modalMovieMeta").textContent = `${currentMovie.genre} • Duration: ${currentMovie.duration}`;
    document.getElementById("couponInput").value = "";
    document.getElementById("couponFeedback").textContent = "";

    buildSeatGrid();
    updatePriceSummary();

    document.getElementById("bookingModalOverlay").classList.add("active");
};

window.closeSeatModal = function() {
    document.getElementById("bookingModalOverlay").classList.remove("active");
};

function buildSeatGrid() {
    const gridContainer = document.getElementById("seatGridContainer");
    gridContainer.innerHTML = "";

    // Collect currently booked seats from static data + localStorage for this show
    const allBookings = getStoredBookings();
    const dynamicBooked = [];
    allBookings.forEach(b => {
        if (b.status === "CONFIRMED" && b.movieTitle === currentMovie.title && b.showTime === currentShow.timeText) {
            b.seatCodes.split(",").forEach(code => dynamicBooked.push(code.trim()));
        }
    });

    const isBooked = (code) => currentShow.bookedSeats.includes(code) || dynamicBooked.includes(code);

    ROWS.forEach(rowLetter => {
        const rowDiv = document.createElement("div");
        rowDiv.className = "seat-row";

        const label = document.createElement("div");
        label.className = "row-label";
        label.textContent = rowLetter;
        rowDiv.appendChild(label);

        const seatsGroup = document.createElement("div");
        seatsGroup.className = "seats-group";

        for (let col = 1; col <= COLS; col++) {
            const code = `${rowLetter}${col}`;
            const seat = document.createElement("div");
            seat.className = "seat";
            seat.textContent = col;
            seat.dataset.code = code;

            if (isBooked(code)) {
                seat.classList.add("booked");
                seat.title = `Seat ${code} is already booked`;
            } else {
                seat.title = `Seat ${code} (${SEAT_TYPES[rowLetter].type} - ₹${SEAT_TYPES[rowLetter].price})`;
                seat.addEventListener("click", () => toggleSeat(code, seat));
            }

            seatsGroup.appendChild(seat);
        }

        rowDiv.appendChild(seatsGroup);

        const tierLabel = document.createElement("div");
        tierLabel.className = "seat-tier-label";
        tierLabel.textContent = `${SEAT_TYPES[rowLetter].type} (₹${SEAT_TYPES[rowLetter].price})`;
        rowDiv.appendChild(tierLabel);

        gridContainer.appendChild(rowDiv);
    });
}

function toggleSeat(code, seatElement) {
    if (selectedSeats.includes(code)) {
        selectedSeats = selectedSeats.filter(s => s !== code);
        seatElement.classList.remove("selected");
    } else {
        selectedSeats.push(code);
        seatElement.classList.add("selected");
    }
    updatePriceSummary();
}

function calculateBasePrice() {
    return selectedSeats.reduce((sum, code) => {
        const rowLetter = code.charAt(0);
        return sum + SEAT_TYPES[rowLetter].price;
    }, 0);
}

function updatePriceSummary() {
    const seatCodesSpan = document.getElementById("selectedSeatsList");
    const baseTotal = calculateBasePrice();

    let discount = 0;
    if (appliedCoupon && appliedCoupon === "CINE10") {
        discount = Math.round(baseTotal * 0.10);
    }
    const finalAmount = baseTotal - discount;

    seatCodesSpan.textContent = selectedSeats.length > 0 ? selectedSeats.join(", ") : "None";
    document.getElementById("totalPriceDisplay").textContent = `₹${finalAmount.toFixed(2)}`;
    
    const discountTag = document.getElementById("discountDisplay");
    if (discount > 0) {
        discountTag.textContent = `(Saved ₹${discount.toFixed(2)} with CINE10)`;
        discountTag.style.display = "inline";
    } else {
        discountTag.style.display = "none";
    }

    const bookBtn = document.getElementById("confirmBookingBtn");
    bookBtn.disabled = selectedSeats.length === 0;
    bookBtn.style.opacity = selectedSeats.length === 0 ? "0.5" : "1";
}

// Coupon validation
window.applyCouponCode = function() {
    const input = document.getElementById("couponInput").value.trim().toUpperCase();
    const feedback = document.getElementById("couponFeedback");

    if (input === "CINE10") {
        appliedCoupon = "CINE10";
        feedback.textContent = "✓ Coupon CINE10 applied! 10% discount granted.";
        feedback.style.color = "var(--accent-emerald)";
    } else if (input === "") {
        appliedCoupon = null;
        feedback.textContent = "Coupon cleared.";
        feedback.style.color = "var(--text-muted)";
    } else {
        appliedCoupon = null;
        feedback.textContent = "✗ Invalid coupon code. Try CINE10";
        feedback.style.color = "#ef4444";
    }
    updatePriceSummary();
};

// =============================================================================
// 4. Confirm Booking & Digital Ticket Display
// =============================================================================

window.processBooking = function() {
    if (selectedSeats.length === 0) return;

    const baseAmount = calculateBasePrice();
    const discount = appliedCoupon === "CINE10" ? Math.round(baseAmount * 0.10) : 0;
    const amountPaid = baseAmount - discount;

    const allBookings = getStoredBookings();
    const nextNum = 1001 + allBookings.length;
    const newId = `BK${nextNum}`;

    const newBooking = {
        bookingId: newId,
        movieTitle: currentMovie.title,
        showTime: currentShow.timeText,
        seatCodes: selectedSeats.join(", "),
        baseAmount: baseAmount,
        discount: discount,
        amountPaid: amountPaid,
        status: "CONFIRMED",
        bookingTime: new Date().toLocaleString(),
        refundAmount: 0
    };

    allBookings.push(newBooking);
    saveBookings(allBookings);

    closeSeatModal();
    showTicketModal(newBooking);
};

function showTicketModal(booking) {
    document.getElementById("ticketId").textContent = booking.bookingId;
    document.getElementById("ticketMovie").textContent = booking.movieTitle;
    document.getElementById("ticketTime").textContent = booking.showTime;
    document.getElementById("ticketSeats").textContent = booking.seatCodes;
    document.getElementById("ticketAmount").textContent = `₹${booking.amountPaid.toFixed(2)}`;
    document.getElementById("ticketStatus").textContent = booking.status;
    document.getElementById("ticketBarcode").textContent = `||| | ||||| | || | ${booking.bookingId} ||`;

    document.getElementById("ticketModalOverlay").classList.add("active");
}

window.closeTicketModal = function() {
    document.getElementById("ticketModalOverlay").classList.remove("active");
};

// =============================================================================
// 5. My Bookings Management (CRUD: READ, UPDATE, CANCEL)
// =============================================================================

function renderBookingsTable() {
    const tableBody = document.getElementById("bookingsTableBody");
    if (!tableBody) return;

    const bookings = getStoredBookings();
    if (bookings.length === 0) {
        tableBody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--text-muted); padding: 30px;">No bookings made yet. Select a movie above to book!</td></tr>`;
        return;
    }

    tableBody.innerHTML = bookings.slice().reverse().map(b => `
        <tr>
            <td><strong>${b.bookingId}</strong></td>
            <td>${b.movieTitle}</td>
            <td>${b.showTime}</td>
            <td>${b.seatCodes}</td>
            <td>₹${b.amountPaid.toFixed(2)}</td>
            <td>
                <span class="${b.status === 'CONFIRMED' ? 'badge-status-confirmed' : 'badge-status-cancelled'}">
                    ${b.status}
                </span>
            </td>
            <td>
                ${b.status === 'CONFIRMED' ? `
                    <button class="btn-cancel-sm" onclick="cancelBookingAction('${b.bookingId}')">
                        Cancel (80% Refund)
                    </button>
                ` : `
                    <span style="color: var(--text-muted); font-size: 0.8rem;">Refund: ₹${b.refundAmount.toFixed(2)}</span>
                `}
            </td>
        </tr>
    `).join('');
}

window.cancelBookingAction = function(bookingId) {
    const bookings = getStoredBookings();
    const booking = bookings.find(b => b.bookingId === bookingId);
    if (!booking) return;

    const refund = Math.round(booking.amountPaid * REFUND_RATE);
    const confirmed = confirm(`Are you sure you want to cancel booking ${bookingId}?\nSeats released: ${booking.seatCodes}\nRefund: ₹${refund.toFixed(2)} (80% of ₹${booking.amountPaid})`);
    
    if (confirmed) {
        booking.status = "CANCELLED";
        booking.refundAmount = refund;
        saveBookings(bookings);
        alert(`Booking ${bookingId} has been cancelled successfully.\n₹${refund.toFixed(2)} refund has been issued.`);
    }
};

// Architecture drawer toggle
window.toggleArchModal = function() {
    const modal = document.getElementById("archModalOverlay");
    modal.classList.toggle("active");
};

function setupEventListeners() {
    // Escape key closes modals
    window.addEventListener("keydown", (e) => {
        if (e.key === "Escape") {
            closeSeatModal();
            closeTicketModal();
            const arch = document.getElementById("archModalOverlay");
            if (arch) arch.classList.remove("active");
        }
    });
}
