/* =========================================================
   LITERARY EMPORIUM - MAIN SCRIPT
   Buyer + Seller + Books + Cart + Wishlist
   ========================================================= */


/* =========================================================
   COMMON MESSAGE
   ========================================================= */

function showMessage(element, message, type) {

    if (!element) {
        return;
    }

    element.textContent = message;

    element.className =
        "validation-message " + (type || "");

}


/* =========================================================
   EMAIL VALIDATION
   ========================================================= */

function isValidEmail(email) {

    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);

}


/* =========================================================
   BUYER STORAGE
   ========================================================= */

function getUsers() {

    return JSON.parse(
        localStorage.getItem(
            "literaryEmporiumUsers"
        )
    ) || [];

}


function saveUsers(users) {

    localStorage.setItem(
        "literaryEmporiumUsers",
        JSON.stringify(users)
    );

}


/* =========================================================
   BUYER LOGIN / REGISTER SWITCH
   ========================================================= */

function showChoice() {

    const choiceSection =
        document.getElementById("choiceSection");

    const loginSection =
        document.getElementById("loginSection");

    const registerSection =
        document.getElementById("registerSection");

    if (choiceSection) {
        choiceSection.style.display = "block";
    }

    if (loginSection) {
        loginSection.style.display = "none";
    }

    if (registerSection) {
        registerSection.style.display = "none";
    }

}


function showLogin() {

    const choiceSection =
        document.getElementById("choiceSection");

    const loginSection =
        document.getElementById("loginSection");

    const registerSection =
        document.getElementById("registerSection");

    if (choiceSection) {
        choiceSection.style.display = "none";
    }

    if (loginSection) {
        loginSection.style.display = "block";
    }

    if (registerSection) {
        registerSection.style.display = "none";
    }

}


function showRegister() {

    const choiceSection =
        document.getElementById("choiceSection");

    const loginSection =
        document.getElementById("loginSection");

    const registerSection =
        document.getElementById("registerSection");

    if (choiceSection) {
        choiceSection.style.display = "none";
    }

    if (loginSection) {
        loginSection.style.display = "none";
    }

    if (registerSection) {
        registerSection.style.display = "block";
    }

}


/* =========================================================
   BUYER REGISTRATION
   ========================================================= */

const registerForm =
    document.getElementById("registerForm");


if (registerForm) {

    registerForm.addEventListener(
        "submit",
        function(event) {

            event.preventDefault();

            const fullName =
                document.getElementById(
                    "fullName"
                )?.value.trim();

            const email =
                document.getElementById(
                    "registerEmail"
                )?.value.trim().toLowerCase();

            const password =
                document.getElementById(
                    "registerPassword"
                )?.value;

            const confirmPassword =
                document.getElementById(
                    "confirmPassword"
                )?.value;

            const message =
                document.getElementById(
                    "registerMessage"
                );


            if (!fullName) {

                showMessage(
                    message,
                    "Please enter your full name.",
                    "error"
                );

                return;
            }


            if (!email || !isValidEmail(email)) {

                showMessage(
                    message,
                    "Please enter a valid email address.",
                    "error"
                );

                return;
            }


            if (!password || password.length < 6) {

                showMessage(
                    message,
                    "Password must contain at least 6 characters.",
                    "error"
                );

                return;
            }


            if (password !== confirmPassword) {

                showMessage(
                    message,
                    "Passwords do not match.",
                    "error"
                );

                return;
            }


            // Connect to Java Servlet backend
            fetch("api/auth/register", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    name: fullName,
                    email: email,
                    password: password,
                    role: "buyer"
                })
            })
            .then(function(res) {
                return res.json();
            })
            .then(function(data) {
                if (data.status === "success") {
                    const users = getUsers();
                    users.push({
                        fullName: fullName,
                        name: fullName,
                        email: email,
                        password: password
                    });
                    saveUsers(users);

                    showMessage(
                        message,
                        "Registration successful. You can now login.",
                        "success"
                    );
                    registerForm.reset();
                } else {
                    showMessage(
                        message,
                        data.message || "Registration failed.",
                        "error"
                    );
                }
            })
            .catch(function(err) {
                // Local fallback
                const users = getUsers();
                const exists = users.some(function(user) {
                    return String(user.email).toLowerCase() === email;
                });
                if (exists) {
                    showMessage(message, "An account with this email already exists.", "error");
                    return;
                }
                users.push({ fullName: fullName, name: fullName, email: email, password: password });
                saveUsers(users);
                showMessage(message, "Registration successful. You can now login.", "success");
                registerForm.reset();
            });

        }
    );

}


/* =========================================================
   BUYER LOGIN
   ========================================================= */

const loginForm =
    document.getElementById("loginForm");


if (loginForm) {

    loginForm.addEventListener(
        "submit",
        function(event) {

            event.preventDefault();

            const email =
                document.getElementById(
                    "loginEmail"
                )?.value.trim().toLowerCase();

            const password =
                document.getElementById(
                    "loginPassword"
                )?.value;

            const message =
                document.getElementById(
                    "loginMessage"
                );


            if (!email || !isValidEmail(email)) {

                showMessage(
                    message,
                    "Please enter a valid email address.",
                    "error"
                );

                return;
            }


            if (!password) {

                showMessage(
                    message,
                    "Please enter your password.",
                    "error"
                );

                return;
            }


            // Connect to Java Servlet backend
            fetch("api/auth/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    email: email,
                    password: password
                })
            })
            .then(function(res) {
                return res.json();
            })
            .then(function(data) {
                if (data.status === "success") {
                    localStorage.setItem(
                        "literaryEmporiumLoggedInUser",
                        JSON.stringify(data.user)
                    );
                    showMessage(
                        message,
                        "Login successful.",
                        "success"
                    );
                    setTimeout(function() {
                        window.location.href = "home.html";
                    }, 600);
                } else {
                    showMessage(
                        message,
                        data.message || "Invalid email or password.",
                        "error"
                    );
                }
            })
            .catch(function() {
                const users = getUsers();
                const user = users.find(function(item) {
                    return String(item.email).toLowerCase() === email && item.password === password;
                });
                if (!user) {
                    showMessage(message, "Invalid email or password.", "error");
                    return;
                }
                localStorage.setItem("literaryEmporiumLoggedInUser", JSON.stringify(user));
                showMessage(message, "Login successful.", "success");
                setTimeout(function() {
                    window.location.href = "home.html";
                }, 600);
            });

        }
    );

}


/* =========================================================
   GET CURRENT BUYER
   ========================================================= */

function getCurrentBuyer() {

    return JSON.parse(
        localStorage.getItem(
            "literaryEmporiumLoggedInUser"
        )
    );

}


/* =========================================================
   HOME SEARCH / CATEGORY
   ========================================================= */

let homeBooks =
    document.querySelectorAll(".book");


let selectedCategory =
    "all";


const searchInput =
    document.getElementById(
        "searchInput"
    );


const categoryButtons =
    document.querySelectorAll(
        ".category"
    );


const bookHeading =
    document.getElementById(
        "bookHeading"
    );


const noResults =
    document.getElementById(
        "noResults"
    );


function updateBooks() {

    homeBooks =
        document.querySelectorAll(
            ".book"
        );


    if (!homeBooks.length) {
        return;
    }


    const searchText =
        searchInput
            ? searchInput.value
                .toLowerCase()
                .trim()
            : "";


    let visibleCount =
        0;


    homeBooks.forEach(
        function(book) {

            const category =
                String(
                    book.dataset.category || ""
                ).toLowerCase();


            const title =
                String(
                    book.dataset.title || ""
                ).toLowerCase();


            const author =
                String(
                    book.dataset.author || ""
                ).toLowerCase();


            const categoryMatch =
                selectedCategory === "all" ||
                category === selectedCategory;


            const searchMatch =
                !searchText ||
                title.includes(searchText) ||
                author.includes(searchText);


            if (
                categoryMatch &&
                searchMatch
            ) {

                book.style.display =
                    "";

                visibleCount++;

            } else {

                book.style.display =
                    "none";

            }

        }
    );


    if (noResults) {

        noResults.style.display =
            visibleCount === 0
                ? "block"
                : "none";

    }

}


/* =========================================================
   CATEGORY BUTTONS
   ========================================================= */

categoryButtons.forEach(
    function(button) {

        button.addEventListener(
            "click",
            function() {

                categoryButtons.forEach(
                    function(item) {

                        item.classList.remove(
                            "active"
                        );

                    }
                );


                button.classList.add(
                    "active"
                );


                selectedCategory =
                    button.dataset.category ||
                    "all";


                if (bookHeading) {

                    if (
                        selectedCategory ===
                        "all"
                    ) {

                        bookHeading.textContent =
                            "Featured Books";

                    } else {

                        let label =
                            button.textContent.trim();

                        bookHeading.textContent =
                            label + " Books";

                    }

                }


                updateBooks();

            }
        );

    }
);


/* =========================================================
   SEARCH
   ========================================================= */

if (searchInput) {

    searchInput.addEventListener(
        "input",
        updateBooks
    );

}


/* =========================================================
   SELLER BOOK STORAGE
   ========================================================= */

function getSellerBooks() {

    return JSON.parse(
        localStorage.getItem(
            "literaryEmporiumSellerBooks"
        )
    ) || [];

}


/* =========================================================
   HTML ESCAPE
   ========================================================= */

function escapeHTML(value) {

    return String(
        value == null ? "" : value
    ).replace(
        /[&<>"']/g,
        function(character) {

            const map = {

                "&": "&amp;",
                "<": "&lt;",
                ">": "&gt;",
                '"': "&quot;",
                "'": "&#39;"

            };

            return map[character];

        }
    );

}


/* =========================================================
   CATEGORY MAP FOR SELLER BOOKS
   ========================================================= */

function normaliseSellerCategory(category) {

    const map = {

        "Fiction":
            "fiction",

        "Non-Fiction":
            "nonfiction",

        "Action / Adventure":
            "action",

        "Romance":
            "romance",

        "Mystery / Thriller":
            "mystery",

        "Science Fiction":
            "science-fiction",

        "Fantasy":
            "fantasy",

        "Biography":
            "biography",

        "History":
            "history",

        "Academic":
            "academic",

        "Self-Help":
            "self-help",

        "Children":
            "children"

    };


    return (
        map[category] ||
        String(category || "")
            .toLowerCase()
            .replace(/\s+/g, "-")
    );

}


/* =========================================================
   DISPLAY SELLER BOOKS ON BUYER HOME
   ========================================================= */

function displaySellerBooksOnHome() {

    const container =
        document.getElementById(
            "sellerBookContainer"
        );

    const emptyMessage =
        document.getElementById(
            "sellerEmptyMessage"
        );

    if (!container) {
        return;
    }

    // Fetch live books from MySQL database via Java Servlet
    fetch("api/books")
        .then(function(res) {
            return res.json();
        })
        .then(function(books) {
            if (Array.isArray(books) && books.length > 0) {
                renderSellerBookCards(books, container, emptyMessage);
                localStorage.setItem(
                    "literaryEmporiumSellerBooks",
                    JSON.stringify(books)
                );
            } else {
                renderSellerBookCards(getSellerBooks(), container, emptyMessage);
            }
        })
        .catch(function() {
            renderSellerBookCards(getSellerBooks(), container, emptyMessage);
        });

}


function renderSellerBookCards(sellerBooks, container, emptyMessage) {

    container.innerHTML = "";

    if (!sellerBooks || sellerBooks.length === 0) {
        if (emptyMessage) {
            emptyMessage.style.display = "block";
        }
        updateHomeBookCollection();
        return;
    }

    if (emptyMessage) {
        emptyMessage.style.display = "none";
    }

    sellerBooks.forEach(
        function(book) {

            const card =
                document.createElement(
                    "article"
                );

            card.className =
                "book seller-book-card";

            card.dataset.category =
                normaliseSellerCategory(
                    book.category
                );

            card.dataset.title =
                book.title || "";

            card.dataset.author =
                book.author || "";

            card.dataset.sellerBookId =
                book.id || "";

            const title =
                escapeHTML(
                    book.title ||
                    "Untitled Book"
                );

            const author =
                escapeHTML(
                    book.author ||
                    "Unknown Author"
                );

            const category =
                escapeHTML(
                    book.category ||
                    "General"
                );

            const description =
                escapeHTML(
                    book.description ||
                    "Seller listed book."
                );

            const price =
                Number(
                    book.price
                ) || 0;

            const imgUrl =
                book.imageUrl ||
                book.image ||
                "";

            let coverHTML = "";

            if (imgUrl) {
                coverHTML = `
                    <img
                        class="book-cover"
                        src="${imgUrl}"
                        alt="Book cover"
                        onerror="this.style.display='none'"
                    >
                `;
            } else {
                coverHTML = `
                    <div class="default-cover">
                        Book Cover
                    </div>
                `;
            }

            card.innerHTML = `
                ${coverHTML}
                <div class="seller-book-label">
                    Database Book
                </div>
                <h3>
                    ${title}
                </h3>
                <p class="author">
                    By ${author}
                </p>
                <p class="description">
                    ${description}
                </p>
                <div class="book-bottom">
                    <span class="price">
                        ₹${price.toFixed(0)}
                    </span>
                    <button
                        type="button"
                        class="view-button seller-view-button"
                    >
                        View Details
                    </button>
                </div>
            `;

            container.appendChild(
                card
            );
        }
    );

    updateHomeBookCollection();

}


/* =========================================================
   REFRESH HOME BOOK COLLECTION
   ========================================================= */

function updateHomeBookCollection() {

    homeBooks =
        document.querySelectorAll(
            ".book"
        );


    updateBooks();

}


/* =========================================================
   SELLER BOOK VIEW DETAILS
   ========================================================= */

document.addEventListener(
    "click",
    function(event) {

        const button =
            event.target.closest(
                ".seller-view-button"
            );


        if (!button) {
            return;
        }


        const card =
            button.closest(
                ".seller-book-card"
            );


        if (!card) {
            return;
        }


        const sellerBookId =
            card.dataset.sellerBookId;


        const sellerBooks =
            getSellerBooks();


        const selectedSellerBook =
            sellerBooks.find(
                function(book) {

                    return String(
                        book.id
                    ) === String(
                        sellerBookId
                    );

                }
            );


        if (!selectedSellerBook) {
            return;
        }


        const selectedBook = {

            title:
                selectedSellerBook.title,

            author:
                selectedSellerBook.author,

            category:
                selectedSellerBook.category,

            description:
                selectedSellerBook.description ||
                "",

            price:
                "₹" +
                Number(
                    selectedSellerBook.price
                ).toFixed(0),

            image:
                selectedSellerBook.image ||
                "",

            sellerEmail:
                selectedSellerBook.sellerEmail ||
                "",

            sellerBookId:
                selectedSellerBook.id ||
                ""

        };


        sessionStorage.setItem(
            "selectedBook",
            JSON.stringify(
                selectedBook
            )
        );


        window.location.href =
            "book-details.html";

    }
);


/* =========================================================
   BOOK DETAILS
   ========================================================= */

const selectedBookData =
    JSON.parse(
        sessionStorage.getItem(
            "selectedBook"
        )
    );


const detailTitle =
    document.getElementById(
        "detailTitle"
    );


const detailAuthor =
    document.getElementById(
        "detailAuthor"
    );


const detailCategory =
    document.getElementById(
        "detailCategory"
    );


const detailDescription =
    document.getElementById(
        "detailDescription"
    );


const detailPrice =
    document.getElementById(
        "detailPrice"
    );


const detailImage =
    document.getElementById(
        "detailImage"
    );


const addToCartButton =
    document.getElementById(
        "addToCartButton"
    );


const addToWishlistButton =
    document.getElementById(
        "addToWishlistButton"
    );


const cartMessage =
    document.getElementById(
        "cartMessage"
    );


if (
    selectedBookData &&
    detailTitle
) {

    detailTitle.textContent =
        selectedBookData.title || "";


    if (detailAuthor) {

        detailAuthor.textContent =
            "By " +
            (selectedBookData.author || "");

    }


    if (detailCategory) {

        detailCategory.textContent =
            selectedBookData.category || "";

    }


    if (detailDescription) {

        detailDescription.textContent =
            selectedBookData.description || "";

    }


    if (detailPrice) {

        detailPrice.textContent =
            selectedBookData.price || "₹0";

    }


    if (
        detailImage &&
        selectedBookData.image
    ) {

        detailImage.src =
            selectedBookData.image;

        detailImage.alt =
            selectedBookData.title || "Book cover";

    }

}


/* =========================================================
   CART STORAGE
   ========================================================= */

function getCart() {

    return JSON.parse(
        localStorage.getItem(
            "literaryEmporiumCart"
        )
    ) || [];

}


function saveCart(cart) {

    localStorage.setItem(
        "literaryEmporiumCart",
        JSON.stringify(cart)
    );

}


/* =========================================================
   ADD TO CART
   ========================================================= */

if (addToCartButton) {

    addToCartButton.addEventListener(
        "click",
        function() {

            if (!selectedBookData) {
                return;
            }


            const cart =
                getCart();


            const price =
                Number(
                    String(
                        selectedBookData.price ||
                        0
                    )
                    .replace("₹", "")
                    .replace(/,/g, "")
                ) || 0;


            const existing =
                cart.find(
                    function(book) {

                        return (
                            book.title ===
                            selectedBookData.title &&
                            book.author ===
                            selectedBookData.author
                        );

                    }
                );


            if (existing) {

                existing.quantity =
                    (Number(
                        existing.quantity
                    ) || 1) + 1;

            } else {

                cart.push({

                    title:
                        selectedBookData.title,

                    author:
                        selectedBookData.author,

                    category:
                        selectedBookData.category,

                    description:
                        selectedBookData.description,

                    price:
                        price,

                    quantity:
                        1,

                    image:
                        selectedBookData.image ||
                        "",

                    sellerEmail:
                        selectedBookData.sellerEmail ||
                        "",

                    sellerBookId:
                        selectedBookData.sellerBookId ||
                        ""

                });

            }


            saveCart(cart);


            showMessage(
                cartMessage,
                "Book added to cart.",
                "success"
            );

        }
    );

}


/* =========================================================
   WISHLIST STORAGE
   ========================================================= */

function getWishlist() {

    return JSON.parse(
        localStorage.getItem(
            "literaryEmporiumWishlist"
        )
    ) || [];

}


function saveWishlist(wishlist) {

    localStorage.setItem(
        "literaryEmporiumWishlist",
        JSON.stringify(wishlist)
    );

}


/* =========================================================
   ADD TO WISHLIST
   ========================================================= */

if (addToWishlistButton) {

    addToWishlistButton.addEventListener(
        "click",
        function() {

            if (!selectedBookData) {
                return;
            }


            const wishlist =
                getWishlist();


            const exists =
                wishlist.some(
                    function(book) {

                        return (
                            book.title ===
                            selectedBookData.title &&
                            book.author ===
                            selectedBookData.author
                        );

                    }
                );


            if (exists) {

                showMessage(
                    cartMessage,
                    "Book is already in your wishlist.",
                    "error"
                );

                return;
            }


            wishlist.push({

                title:
                    selectedBookData.title,

                author:
                    selectedBookData.author,

                category:
                    selectedBookData.category,

                description:
                    selectedBookData.description,

                price:
                    selectedBookData.price,

                image:
                    selectedBookData.image ||
                    "",

                sellerEmail:
                    selectedBookData.sellerEmail ||
                    "",

                sellerBookId:
                    selectedBookData.sellerBookId ||
                    ""

            });


            saveWishlist(
                wishlist
            );


            showMessage(
                cartMessage,
                "Book added to wishlist.",
                "success"
            );

        }
    );

}


/* =========================================================
   CART DISPLAY
   ========================================================= */

function displayCart() {

    const cartContainer =
        document.getElementById("cartContainer") ||
        document.getElementById("cartItems");

    const emptyCartMessage =
        document.getElementById("emptyCartMessage") ||
        document.getElementById("emptyCart");

    const cartTotal =
        document.getElementById("cartTotal") ||
        document.getElementById("total");

    const subtotalElement =
        document.getElementById("subtotal");

    const itemCount =
        document.getElementById("itemCount") ||
        document.getElementById("cartItemCount") ||
        document.getElementById("itemsCount");


    if (!cartContainer) {
        return;
    }


    const cart =
        JSON.parse(
            localStorage.getItem(
                "literaryEmporiumCart"
            )
        ) || [];


    cartContainer.innerHTML = "";


    if (cart.length === 0) {

        if (emptyCartMessage) {
            emptyCartMessage.style.display = "block";
        }

        if (cartTotal) {
            cartTotal.textContent = "₹0.00";
        }

        if (subtotalElement) {
            subtotalElement.textContent = "₹0.00";
        }

        if (itemCount) {
            itemCount.textContent = "0";
        }

        return;
    }


    if (emptyCartMessage) {
        emptyCartMessage.style.display = "none";
    }


    let total = 0;
    let totalItems = 0;


    cart.forEach(function(book, index) {

        const price =
            Number(
                String(book.price || 0)
                    .replace("₹", "")
                    .replace(/,/g, "")
                    .replace(/[^\d.]/g, "")
            ) || 0;


        const quantity =
            Number(book.quantity) || 1;


        const itemTotal =
            price * quantity;


        total += itemTotal;
        totalItems += quantity;


        const cartItem =
            document.createElement("div");


        cartItem.className =
            "cart-item";


        const imageHTML =
            book.image
                ? `
                    <img
                        src="${book.image}"
                        alt="Book cover"
                        style="
                            width:80px;
                            height:110px;
                            object-fit:cover;
                            display:block;
                            margin-bottom:12px;
                        "
                    >
                  `
                : "";


        cartItem.innerHTML = `

            ${imageHTML}

            <div class="book-info">

                <h3>
                    ${book.title || "Untitled Book"}
                </h3>

                <p>
                    By ${book.author || "Unknown Author"}
                </p>

                <p>
                    ${book.category || ""}
                </p>

                <p>
                    ₹${price.toFixed(2)}
                </p>

            </div>


            <div class="cart-controls">

                <button
                    type="button"
                    onclick="decreaseQuantity(${index})"
                >
                    −
                </button>

                <span>
                    ${quantity}
                </span>

                <button
                    type="button"
                    onclick="increaseQuantity(${index})"
                >
                    +
                </button>

                <button
                    type="button"
                    onclick="removeFromCart(${index})"
                >
                    Remove
                </button>

            </div>


            <div class="book-price">
                ₹${itemTotal.toFixed(2)}
            </div>

        `;


        cartContainer.appendChild(cartItem);

    });


    if (cartTotal) {
        cartTotal.textContent =
            "₹" + total.toFixed(2);
    }


    if (subtotalElement) {
        subtotalElement.textContent =
            "₹" + total.toFixed(2);
    }


    if (itemCount) {
        itemCount.textContent =
            totalItems;
    }

}


function increaseQuantity(index) {

    const cart =
        getCart();


    if (!cart[index]) {
        return;
    }


    cart[index].quantity =
        (Number(
            cart[index].quantity
        ) || 1) + 1;


    saveCart(cart);

    displayCart();

}


function decreaseQuantity(index) {

    const cart =
        getCart();


    if (!cart[index]) {
        return;
    }


    const quantity =
        Number(
            cart[index].quantity
        ) || 1;


    if (quantity > 1) {

        cart[index].quantity =
            quantity - 1;

    } else {

        cart.splice(
            index,
            1
        );

    }


    saveCart(cart);

    displayCart();

}


function removeFromCart(index) {

    const cart =
        getCart();


    if (!cart[index]) {
        return;
    }


    cart.splice(
        index,
        1
    );


    saveCart(cart);

    displayCart();

}


/* =========================================================
   WISHLIST DISPLAY
   ========================================================= */

function displayWishlist() {

    const container =
        document.getElementById(
            "wishlistContainer"
        );


    const emptyMessage =
        document.getElementById(
            "emptyWishlistMessage"
        );


    if (!container) {
        return;
    }


    const wishlist =
        getWishlist();


    container.innerHTML = "";


    if (wishlist.length === 0) {

        if (emptyMessage) {

            emptyMessage.style.display =
                "block";

        }


        return;

    }


    if (emptyMessage) {

        emptyMessage.style.display =
            "none";

    }


    wishlist.forEach(
        function(book, index) {

            const item =
                document.createElement(
                    "div"
                );


            item.className =
                "wishlist-item";


            item.innerHTML = `

                <div>

                    <h3>
                        ${escapeHTML(
                            book.title
                        )}
                    </h3>

                    <p>
                        By ${escapeHTML(
                            book.author
                        )}
                    </p>

                    <p>
                        ${escapeHTML(
                            book.category || ""
                        )}
                    </p>

                    <p>
                        ${escapeHTML(
                            String(
                                book.price || ""
                            )
                        )}
                    </p>

                </div>

                <button
                    type="button"
                    onclick="removeFromWishlist(${index})"
                >
                    Remove
                </button>

            `;


            container.appendChild(
                item
            );

        }
    );

}


function removeFromWishlist(index) {

    const wishlist =
        getWishlist();


    if (!wishlist[index]) {
        return;
    }


    wishlist.splice(
        index,
        1
    );


    saveWishlist(
        wishlist
    );


    displayWishlist();

}


/* =========================================================
   SELLER STORAGE
   ========================================================= */

function getSellers() {

    return JSON.parse(
        localStorage.getItem(
            "literaryEmporiumSellers"
        )
    ) || [];

}


function saveSellers(sellers) {

    localStorage.setItem(
        "literaryEmporiumSellers",
        JSON.stringify(sellers)
    );

}


/* =========================================================
   SELLER REGISTRATION
   ========================================================= */

const sellerRegisterForm =
    document.getElementById(
        "sellerRegisterForm"
    );


if (sellerRegisterForm) {

    sellerRegisterForm.addEventListener(
        "submit",
        function(event) {

            event.preventDefault();


            const name =
                document.getElementById(
                    "sellerName"
                )?.value.trim();


            const email =
                document.getElementById(
                    "sellerEmail"
                )?.value.trim().toLowerCase();


            const password =
                document.getElementById(
                    "sellerPassword"
                )?.value;


            const confirmPassword =
                document.getElementById(
                    "sellerConfirmPassword"
                )?.value;


            const message =
                document.getElementById(
                    "sellerRegisterMessage"
                );


            if (!name) {

                showMessage(
                    message,
                    "Please enter your name.",
                    "error"
                );

                return;
            }


            if (
                !email ||
                !isValidEmail(email)
            ) {

                showMessage(
                    message,
                    "Please enter a valid email address.",
                    "error"
                );

                return;
            }


            if (
                !password ||
                password.length < 6
            ) {

                showMessage(
                    message,
                    "Password must contain at least 6 characters.",
                    "error"
                );

                return;
            }


            if (
                password !==
                confirmPassword
            ) {

                showMessage(
                    message,
                    "Passwords do not match.",
                    "error"
                );

                return;
            }


            // Connect to Java Servlet backend
            fetch("api/auth/register", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    name: name,
                    email: email,
                    password: password,
                    role: "seller"
                })
            })
            .then(function(res) {
                return res.json();
            })
            .then(function(data) {
                if (data.status === "success") {
                    const sellers = getSellers();
                    sellers.push({
                        name: name,
                        email: email,
                        password: password
                    });
                    saveSellers(sellers);

                    showMessage(
                        message,
                        "Seller registration successful.",
                        "success"
                    );
                    sellerRegisterForm.reset();
                } else {
                    showMessage(
                        message,
                        data.message || "Seller registration failed.",
                        "error"
                    );
                }
            })
            .catch(function() {
                const sellers = getSellers();
                const exists = sellers.some(function(seller) {
                    return String(seller.email).toLowerCase() === email;
                });
                if (exists) {
                    showMessage(message, "An account with this email already exists.", "error");
                    return;
                }
                sellers.push({ name: name, email: email, password: password });
                saveSellers(sellers);
                showMessage(message, "Seller registration successful.", "success");
                sellerRegisterForm.reset();
            });

        }
    );

}


/* =========================================================
   SELLER LOGIN
   ========================================================= */

const sellerLoginForm =
    document.getElementById(
        "sellerLoginForm"
    );


if (sellerLoginForm) {

    sellerLoginForm.addEventListener(
        "submit",
        function(event) {

            event.preventDefault();


            const email =
                document.getElementById(
                    "sellerLoginEmail"
                )?.value.trim().toLowerCase();


            const password =
                document.getElementById(
                    "sellerLoginPassword"
                )?.value;


            const message =
                document.getElementById(
                    "sellerLoginMessage"
                );


            if (!email || !isValidEmail(email)) {

                showMessage(
                    message,
                    "Please enter a valid email address.",
                    "error"
                );

                return;
            }


            if (!password) {

                showMessage(
                    message,
                    "Please enter your password.",
                    "error"
                );

                return;
            }


            // Connect to Java Servlet backend
            fetch("api/auth/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    email: email,
                    password: password
                })
            })
            .then(function(res) {
                return res.json();
            })
            .then(function(data) {
                if (data.status === "success") {
                    localStorage.setItem(
                        "literaryEmporiumCurrentSeller",
                        JSON.stringify(data.user)
                    );
                    showMessage(
                        message,
                        "Login successful.",
                        "success"
                    );
                    setTimeout(function() {
                        window.location.href = "seller-dashboard.html";
                    }, 600);
                } else {
                    showMessage(
                        message,
                        data.message || "Invalid email or password.",
                        "error"
                    );
                }
            })
            .catch(function() {
                const sellers = getSellers();
                const seller = sellers.find(function(item) {
                    return String(item.email).toLowerCase() === email && item.password === password;
                });
                if (!seller) {
                    showMessage(message, "Invalid email or password.", "error");
                    return;
                }
                localStorage.setItem("literaryEmporiumCurrentSeller", JSON.stringify(seller));
                showMessage(message, "Login successful.", "success");
                setTimeout(function() {
                    window.location.href = "seller-dashboard.html";
                }, 600);
            });

        }
    );

}


/* =========================================================
   LOGOUT LINKS
   ========================================================= */

document.addEventListener(
    "click",
    function(event) {

        const logoutLink =
            event.target.closest(
                "#logoutLink"
            );


        if (!logoutLink) {
            return;
        }


        event.preventDefault();


        localStorage.removeItem(
            "literaryEmporiumLoggedInUser"
        );


        window.location.href =
            "login.html";

    }
);


/* =========================================================
   INITIAL LOAD
   ========================================================= */

document.addEventListener(
    "DOMContentLoaded",
    function() {

        displaySellerBooksOnHome();

        displayCart();

        displayWishlist();

        updateHomeBookCollection();

    }
);