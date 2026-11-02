/**
 * CineBook - Movie Ticket Booking System
 * Interactive Frontend Controller (Vanilla JavaScript)
 * Demonstrates OOP models, dynamic 5x8 seat mapping, coupon calculation, and CRUD operations.
 * Featuring 5 Tamil Blockbuster Movies:
 *  1. Baththa (Tamil)
 *  2. Dorothy (Tamil)
 *  3. Meesaya Murukku 2 (Tamil)
 *  4. Jailer 2 (Tamil)
 *  5. Mandaadi (Tamil)
 */

// =============================================================================
// 1. Data Store - 5 Tamil Movies (Matches Java Backend Domain Model)
// =============================================================================

const MOVIES = [
    {
        id: 1,
        title: "Baththa",
        language: "Tamil",
        genre: "Comedy / Crime / Drama",
        duration: "142 mins",
        releaseDate: "October 1, 2026",
        rating: "8.8",
        director: "Balaji Tharaneetharan",
        cast: "Vijay Sethupathi, Master Sai Prithviraj, Lijomol Jose",
        music: "Sai Abhyankkar",
        synopsis: "Set in 1986 Madras, a feared local gangster named Baththa gets accidentally locked inside an abandoned school restroom by an 8-year-old schoolboy. Isolated and stripped of his power, a heartwarming bond forms that alters his life.",
        poster: "images/baththa.jpeg",
        shows: [
            { id: 101, timeText: "10:30 AM", bookedSeats: ["A2", "B4", "C3"] },
            { id: 102, timeText: "6:45 PM",  bookedSeats: ["C1", "C2", "E3", "E4"] }
        ]
    },
    {
        id: 2,
        title: "Dorothy",
        language: "Tamil",
        genre: "Crime / Drama",
        duration: "154 mins",
        releaseDate: "September 25, 2026",
        rating: "8.7",
        director: "Karthik Subbaraj",
        cast: "Keerthy Suresh, Sananth, Rishikanth",
        music: "Ilaiyaraaja",
        synopsis: "Set against rural Tamil Nadu in the early 1990s, two childhood friends find their lives transformed by the gentle empathy of Dorothy. When a tragic event occurs, loyalty, friendship, and societal norms are put to the ultimate test.",
        poster: "images/dorothy.jpeg",
        shows: [
            { id: 201, timeText: "1:45 PM", bookedSeats: ["B2", "D5"] },
            { id: 202, timeText: "7:15 PM", bookedSeats: ["A1", "C4", "E5"] }
        ]
    },
    {
        id: 3,
        title: "Meesaya Murukku 2",
        language: "Tamil",
        genre: "Musical / Comedy / Youth Drama",
        duration: "148 mins",
        releaseDate: "2026",
        rating: "8.4",
        director: "Hiphop Tamizha Adhi",
        cast: "Hiphop Tamizha Adhi, Ketika Sharma, Chaithra J Achar",
        music: "Hiphop Tamizha",
        synopsis: "The energetic musical successor celebrating passionate independent musicians, college youth culture, and Chennai's vibrant Gaana music legacy across generations.",
        poster: "images/meesaya_murukku_2.jpeg",
        shows: [
            { id: 301, timeText: "11:15 AM", bookedSeats: ["A1", "A3", "B6"] },
            { id: 302, timeText: "4:30 PM",  bookedSeats: ["C2", "D3", "E2"] }
        ]
    },
    {
        id: 4,
        title: "Jailer 2",
        language: "Tamil",
        genre: "Action / Crime / Thriller",
        duration: "168 mins",
        releaseDate: "2026",
        rating: "9.2",
        director: "Nelson Dilipkumar",
        cast: "Superstar Rajinikanth, S. J. Suryah, Vidya Balan, Mithun Chakraborty",
        music: "Anirudh Ravichander",
        synopsis: "'Tiger' Muthuvel Pandian returns for an explosive clash against a ruthless international crime syndicate, bringing back his lethal calm, razor-sharp tactical brilliance, and dark humor.",
        poster: "images/jailer_2.jpeg",
        shows: [
            { id: 401, timeText: "10:00 AM", bookedSeats: ["E1", "E2", "C3", "C4"] },
            { id: 402, timeText: "2:30 PM",  bookedSeats: ["D1", "D2"] },
            { id: 403, timeText: "7:00 PM",  bookedSeats: ["A4", "B3", "E5"] },
            { id: 404, timeText: "10:15 PM", bookedSeats: ["C5", "C6", "D7"] }
        ]
    },
    {
        id: 5,
        title: "Mandaadi",
        language: "Tamil",
        genre: "Sports / Action / Drama",
        duration: "138 mins",
        releaseDate: "2026",
        rating: "8.5",
        director: "Mathimaran Pugazhendhi",
        cast: "Soori, Suhas, Mahima Nambiar",
        music: "GV Prakash Kumar",
        synopsis: "Set along the rugged coastal shores of Tamil Nadu, local fishermen rally to defend their livelihoods and maritime heritage through intense, high-stakes traditional ocean boat racing (Mandaadi).",
        poster: "images/mandaadi.jpeg",
        shows: [
            { id: 501, timeText: "1:00 PM", bookedSeats: ["A5", "B1"] },
            { id: 502, timeText: "8:30 PM", bookedSeats: ["C7", "D8", "E1"] }
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
    const existing = localStorage.getItem("cinebook_web_bookings");
    if (!existing || existing.includes("Interstellar") || existing.includes("Baattha")) {
        const initialBookings = [
            {
                bookingId: "BK1001",
                movieTitle: "Baththa (Tamil)",
                showTime: "6:45 PM",
                seatCodes: "C3, C4",
                baseAmount: 500,
                discount: 50,
                amountPaid: 450,
                status: "CONFIRMED",
                bookingTime: "2026-11-02 14:15",
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

// Render the 5 Tamil movies grid
function renderMovieCards() {
    const container = document.getElementById("moviesGrid");
    if (!container) return;

    container.innerHTML = MOVIES.map(movie => `
        <div class="movie-card">
            <div class="movie-poster-box" style="background-image: url('${movie.poster}')">
                <div class="badge-row">
                    <span class="rating-badge">★ ${movie.rating}</span>
                    <div style="display: flex; gap: 6px;">
                        <span class="lang-badge">${movie.language}</span>
                        <span class="genre-badge">${movie.genre}</span>
                    </div>
                </div>
                <div class="movie-info">
                    <h3 class="movie-title">${movie.title}</h3>
                    <p class="movie-meta">${movie.duration} • Directed by ${movie.director}</p>
                </div>
            </div>
            <div class="movie-body">
                <div>
                    <p class="movie-synopsis">${movie.synopsis}</p>
                    <div style="font-size: 0.8rem; color: var(--text-muted); margin-bottom: 12px;">
                        <strong>Starring:</strong> ${movie.cast}
                    </div>
                </div>
                <div>
                    <div class="showtimes-label">Select Show Time:</div>
                    <div class="showtime-chips">
                        ${movie.shows.map(show => `
                            <button class="show-chip" onclick="openSeatModal(${movie.id}, ${show.id})">
                                🕒 ${show.timeText}
                            </button>
                        `).join('')}
                    </div>
                    <button class="btn-primary" style="width: 100%; margin-top: 10px; justify-content: center;" onclick="openSeatModal(${movie.id}, ${movie.shows[0].id})">
                        🎟️ Book Tickets (${movie.shows[0].timeText})
                    </button>
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

    document.getElementById("modalMovieTitle").textContent = `${currentMovie.title} (${currentMovie.language}) - ${currentShow.timeText}`;
    document.getElementById("modalMovieMeta").textContent = `${currentMovie.genre} • Duration: ${currentMovie.duration} • Director: ${currentMovie.director}`;
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

    const allBookings = getStoredBookings();
    const dynamicBooked = [];
    allBookings.forEach(b => {
        const fullTitle = `${currentMovie.title} (${currentMovie.language})`;
        if (b.status === "CONFIRMED" && (b.movieTitle === currentMovie.title || b.movieTitle === fullTitle) && b.showTime === currentShow.timeText) {
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
                seat.title = `Seat ${code} is already booked in this show`;
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
        movieTitle: `${currentMovie.title} (${currentMovie.language})`,
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
        tableBody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--text-muted); padding: 30px;">No bookings made yet. Select a Tamil movie above to book!</td></tr>`;
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
    const confirmed = confirm(`Are you sure you want to cancel booking ${bookingId}?\nMovie: ${booking.movieTitle}\nSeats released: ${booking.seatCodes}\nRefund: ₹${refund.toFixed(2)} (80% of ₹${booking.amountPaid})`);
    
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
    window.addEventListener("keydown", (e) => {
        if (e.key === "Escape") {
            closeSeatModal();
            closeTicketModal();
            const arch = document.getElementById("archModalOverlay");
            if (arch) arch.classList.remove("active");
        }
    });
}
