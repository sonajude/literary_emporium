(function () {

    /* =========================================================
       LITERARY EMPORIUM
       PROFESSIONAL 600-BOOK CATALOGUE
       ========================================================= */

    const CATALOGUE_CATEGORIES = [
        { id: "fiction", label: "Fiction", queries: ["fiction"] },
        { id: "nonfiction", label: "Non-Fiction", queries: ["nonfiction"] },
        { id: "action", label: "Action / Adventure", queries: ["adventure fiction", "action fiction"] },
        { id: "romance", label: "Romance", queries: ["romance"] },
        { id: "mystery", label: "Mystery / Thriller", queries: ["mystery", "detective fiction", "thriller"] },
        { id: "science-fiction", label: "Science Fiction", queries: ["science fiction"] },
        { id: "fantasy", label: "Fantasy", queries: ["fantasy fiction"] },
        { id: "biography", label: "Biography", queries: ["biography", "autobiography"] },
        { id: "history", label: "History", queries: ["history"] },
        { id: "academic", label: "Academic", queries: ["textbook", "academic"] },
        { id: "self-help", label: "Self-Help", queries: ["self help", "personal development"] },
        { id: "children", label: "Children", queries: ["children's books", "juvenile fiction"] }
    ];

    const CATALOGUE_API =
        "https://openlibrary.org/search.json";

    const BOOKS_PER_CATEGORY = 50;
    const TOTAL_BOOKS = 600;

    const catalogueBooks = [];

    let catalogueActiveCategory = "all";
    let catalogueSearchTerm = "";

    const getElement = id =>
        document.getElementById(id);


    /* =========================================================
       PRICE
       ========================================================= */

    function getBookPrice(category, index) {

        const bases = {
            fiction: 399,
            nonfiction: 449,
            action: 499,
            romance: 399,
            mystery: 479,
            "science-fiction": 549,
            fantasy: 499,
            biography: 449,
            history: 529,
            academic: 599,
            "self-help": 449,
            children: 349
        };

        const base = bases[category] || 399;

        return base + ((index * 37) % 260);
    }


    /* =========================================================
       CLEAN TEXT
       ========================================================= */

    function cleanText(value) {
        return String(value ?? "").trim();
    }


    /* =========================================================
       UNIQUE KEY
       ========================================================= */

    function makeBookKey(book) {

        return cleanText(book.key || book.title)
            .toLowerCase()
            .replace(/[^a-z0-9]+/g, "");
    }


    /* =========================================================
       ESCAPE HTML
       ========================================================= */

    function escapeBookHTML(value) {

        return String(value ?? "").replace(
            /[&<>'"]/g,
            character => ({
                "&": "&amp;",
                "<": "&lt;",
                ">": "&gt;",
                "'": "&#39;",
                '"': "&quot;"
            }[character])
        );
    }


    /* =========================================================
       FETCH OPEN LIBRARY
       ========================================================= */

    async function fetchBooks(query, offset = 0) {

        const fields =
            "key,title,author_name,first_publish_year,cover_i,isbn,first_sentence";

        const url =
            CATALOGUE_API +
            "?subject=" +
            encodeURIComponent(query) +
            "&limit=50" +
            "&offset=" +
            offset +
            "&fields=" +
            fields;

        const response =
            await fetch(url);

        if (!response.ok) {
            throw new Error("Open Library request failed");
        }

        return await response.json();
    }


    /* =========================================================
       CREATE BOOK OBJECT
       ========================================================= */

    function createBookObject(doc, category, index) {

        const title =
            cleanText(doc.title) ||
            "Untitled Book";

        const authors =
            Array.isArray(doc.author_name)
                ? doc.author_name.filter(Boolean)
                : [];

        let description = "";

        if (Array.isArray(doc.first_sentence)) {
            description =
                cleanText(doc.first_sentence[0]);
        } else {
            description =
                cleanText(doc.first_sentence);
        }

        if (!description) {
            description =
                "Book information and publication details are available in the Literary Emporium catalogue.";
        }

        const categoryObject =
            CATALOGUE_CATEGORIES.find(
                categoryItem =>
                    categoryItem.id === category
            );

        return {

            id:
                makeBookKey(doc) +
                "-" +
                category +
                "-" +
                index,

            sourceKey:
                cleanText(doc.key),

            title:
                title,

            author:
                authors.length
                    ? authors.join(", ")
                    : "Author information unavailable",

            year:
                doc.first_publish_year || "—",

            coverId:
                doc.cover_i || null,

            isbn:
                Array.isArray(doc.isbn)
                    ? doc.isbn[0] || null
                    : null,

            description:
                description,

            category:
                category,

            categoryLabel:
                categoryObject
                    ? categoryObject.label
                    : category,

            price:
                getBookPrice(
                    category,
                    index
                )
        };
    }


    /* =========================================================
       LOAD CATEGORY
       ========================================================= */

    async function loadOneCategory(category) {

        const categoryObject =
            CATALOGUE_CATEGORIES.find(
                item => item.id === category
            );

        const books = [];
        const seen = new Set();

        for (
            const query of categoryObject.queries
        ) {

            if (
                books.length >=
                BOOKS_PER_CATEGORY
            ) {
                break;
            }

            for (
                let offset = 0;
                offset <= 150;
                offset += 50
            ) {

                if (
                    books.length >=
                    BOOKS_PER_CATEGORY
                ) {
                    break;
                }

                try {

                    const data =
                        await fetchBooks(
                            query,
                            offset
                        );

                    const documents =
                        data.docs || [];

                    for (
                        const document of documents
                    ) {

                        if (!document.title) {
                            continue;
                        }

                        const book =
                            createBookObject(
                                document,
                                category,
                                books.length
                            );

                        const uniqueKey =
                            (
                                cleanText(document.key) ||
                                (
                                    book.title +
                                    "|" +
                                    book.author
                                )
                            ).toLowerCase();

                        if (
                            seen.has(uniqueKey)
                        ) {
                            continue;
                        }

                        seen.add(uniqueKey);

                        books.push(book);

                        if (
                            books.length >=
                            BOOKS_PER_CATEGORY
                        ) {
                            break;
                        }
                    }

                    if (
                        documents.length < 50
                    ) {
                        break;
                    }

                } catch (error) {

                    console.warn(
                        "Could not load category:",
                        category,
                        error
                    );

                    break;
                }
            }
        }

        return books;
    }


    /* =========================================================
       STATUS
       ========================================================= */

    function setCatalogueStatus(message) {

        const status =
            getElement("catalogueStatus");

        if (status) {
            status.textContent = message;
        }
    }


    /* =========================================================
       COVER
       ========================================================= */

  function getCoverURL(book) {

    if (book.coverId) {
        return `https://covers.openlibrary.org/b/id/${book.coverId}-M.jpg`;
    }

    if (book.isbn) {
        return `https://covers.openlibrary.org/b/isbn/${encodeURIComponent(book.isbn)}-M.jpg`;
    }

    return "";
}
    /* =========================================================
       LOADING
       ========================================================= */

    function showCatalogueLoading() {

        const container =
            getElement("bookContainer");

        if (!container) {
            return;
        }

        container.innerHTML = Array.from(
            { length: 12 },
            () => `
                <div class="loading-card">
                    Loading book catalogue...
                </div>
            `
        ).join("");
    }


    /* =========================================================
       BOOK CARD
       ========================================================= */

    function createCatalogueCard(book) {

        const title =
            escapeBookHTML(book.title);

        const author =
            escapeBookHTML(book.author);

        const description =
            escapeBookHTML(book.description);

        const cover =
            getCoverURL(book);

        return `
            <article class="book-card">

                <div class="book-cover-wrap">

                    ${
                        cover
                            ? `
                                <img
                                    class="book-cover"
                                    src="${cover}"
                                    alt="Cover of ${title}"
                                    loading="lazy"
                                    onerror="
                                        this.style.display='none';
                                        this.nextElementSibling.style.display='flex';
                                    "
                                >
                            `
                            : ""
                    }

                    <div
                        class="book-cover-fallback"
                        style="${cover ? "display:none" : "display:flex"}"
                    >
                        ${title}
                    </div>

                </div>


                <div class="book-body">

                    <div class="book-category">
                        ${escapeBookHTML(book.categoryLabel)}
                    </div>

                    <h3 class="book-title">
                        ${title}
                    </h3>

                    <div class="book-author">
                        ${author}
                    </div>

                    <div class="book-meta">

                        <span>
                            First published
                        </span>

                        <span>
                            ${escapeBookHTML(book.year)}
                        </span>

                    </div>

                    <div class="book-description">
                        ${description}
                    </div>

                    <div class="book-price">
                        ₹${book.price.toLocaleString("en-IN")}
                    </div>

                    <button
                        class="view-button"
                        data-book-id="${escapeBookHTML(book.id)}"
                    >
                        View Book
                    </button>

                </div>

            </article>
        `;
    }


    /* =========================================================
       RENDER
       ========================================================= */

    function renderCatalogue() {

        const container =
            getElement("bookContainer");

        if (!container) {
            return;
        }

        const term =
            catalogueSearchTerm.toLowerCase();

        const visibleBooks =
            catalogueBooks.filter(book => {

                const categoryMatches =
                    catalogueActiveCategory === "all" ||
                    book.category ===
                        catalogueActiveCategory;

                const searchMatches =
                    !term ||
                    book.title
                        .toLowerCase()
                        .includes(term) ||
                    book.author
                        .toLowerCase()
                        .includes(term);

                return (
                    categoryMatches &&
                    searchMatches
                );
            });


        container.innerHTML =
            visibleBooks.length
                ? visibleBooks
                    .map(createCatalogueCard)
                    .join("")
                : "";


        const noResults =
            getElement("noResults");

        if (noResults) {

            noResults.hidden =
                visibleBooks.length !== 0;
        }


        const heading =
            getElement("bookHeading");

        if (heading) {

            if (
                catalogueActiveCategory ===
                "all"
            ) {

                heading.textContent =
                    "All Books";

            } else {

                const category =
                    CATALOGUE_CATEGORIES.find(
                        item =>
                            item.id ===
                            catalogueActiveCategory
                    );

                heading.textContent =
                    category
                        ? category.label
                        : "Books";
            }
        }


        const resultCount =
            getElement("resultCount");

        if (resultCount) {

            resultCount.textContent =
                `${visibleBooks.length} shown · ${catalogueBooks.length} loaded`;
        }


        document
            .querySelectorAll(".category")
            .forEach(button => {

                button.classList.toggle(
                    "active",
                    button.dataset.category ===
                        catalogueActiveCategory
                );
            });
    }


    /* =========================================================
       OPEN BOOK
       ========================================================= */

    function openCatalogueBook(bookId) {

        const book =
            catalogueBooks.find(
                item =>
                    item.id === bookId
            );

        if (!book) {
            return;
        }

        sessionStorage.setItem(
            "selectedBook",
            JSON.stringify({

                title:
                    book.title,

                author:
                    book.author,

                price:
                    book.price,

                category:
                    book.category,

                categoryLabel:
                    book.categoryLabel,

                year:
                    book.year,

                cover:
                    getCoverURL(book),

                description:
                    book.description,

                sourceKey:
                    book.sourceKey
            })
        );

        window.location.href =
            "book-details.html";
    }


    /* =========================================================
       SAVE
       ========================================================= */

    function saveCatalogue() {

        localStorage.setItem(
            "literaryEmporiumCatalogue",
            JSON.stringify(
                catalogueBooks
            )
        );
    }


    /* =========================================================
       LOAD 600 BOOKS
       ========================================================= */

    async function loadFullCatalogue() {

        showCatalogueLoading();

        setCatalogueStatus(
            "Loading 600 books..."
        );


        const categoryResults =
            await Promise.all(
                CATALOGUE_CATEGORIES.map(
                    async category => {

                        const books =
                            await loadOneCategory(
                                category.id
                            );

                        setCatalogueStatus(
                            `${category.label}: ${books.length}/${BOOKS_PER_CATEGORY} loaded`
                        );

                        return books;
                    }
                )
            );


        catalogueBooks.length = 0;


        categoryResults.forEach(
            books => {

                catalogueBooks.push(
                    ...books
                );
            }
        );


        /*
           Remove duplicate IDs
        */

        const uniqueBooks =
            new Map();

        catalogueBooks.forEach(
            book => {

                if (
                    !uniqueBooks.has(
                        book.id
                    )
                ) {

                    uniqueBooks.set(
                        book.id,
                        book
                    );
                }
            }
        );


        catalogueBooks.length = 0;

        catalogueBooks.push(
            ...uniqueBooks.values()
        );


        saveCatalogue();

        setCatalogueStatus(
            `${catalogueBooks.length} books available`
        );

        renderCatalogue();
    }


    /* =========================================================
       SEARCH
       ========================================================= */

    const catalogueSearchInput =
        getElement("searchInput");

    if (catalogueSearchInput) {

        catalogueSearchInput.addEventListener(
            "input",
            event => {

                catalogueSearchTerm =
                    event.target.value.trim();

                renderCatalogue();
            }
        );
    }


    /* =========================================================
       CATEGORY BUTTONS
       ========================================================= */

    const catalogueCategoryBar =
        getElement("categoryBar");

    if (catalogueCategoryBar) {

        catalogueCategoryBar.addEventListener(
            "click",
            event => {

                const button =
                    event.target.closest(
                        ".category"
                    );

                if (!button) {
                    return;
                }

                catalogueActiveCategory =
                    button.dataset.category;

                renderCatalogue();
            }
        );
    }


    /* =========================================================
       BOOK CLICK
       ========================================================= */

    const catalogueBookContainer =
        getElement("bookContainer");

    if (catalogueBookContainer) {

        catalogueBookContainer.addEventListener(
            "click",
            event => {

                const button =
                    event.target.closest(
                        ".view-button"
                    );

                if (!button) {
                    return;
                }

                openCatalogueBook(
                    button.dataset.bookId
                );
            }
        );
    }


    /* =========================================================
       LOAD SAVED DATA
       ========================================================= */

    try {

        const savedCatalogue =
            JSON.parse(
                localStorage.getItem(
                    "literaryEmporiumCatalogue"
                ) || "[]"
            );


        if (
            Array.isArray(savedCatalogue) &&
            savedCatalogue.length > 0
        ) {

            catalogueBooks.push(
                ...savedCatalogue
            );

            setCatalogueStatus(
                `${catalogueBooks.length} saved books available`
            );

            renderCatalogue();
        }

    } catch (error) {

        console.warn(
            "Could not read saved catalogue",
            error
        );
    }


    /* =========================================================
       REFRESH ONLY IF LESS THAN 600
       ========================================================= */

    if (
        catalogueBooks.length <
        TOTAL_BOOKS
    ) {

        loadFullCatalogue()
            .catch(error => {

                console.error(
                    "Catalogue error:",
                    error
                );

                if (
                    catalogueBooks.length === 0
                ) {

                    const container =
                        getElement(
                            "bookContainer"
                        );

                    if (container) {

                        container.innerHTML = `
                            <div class="no-results">
                                The book catalogue could not be loaded.
                                Please check your internet connection
                                and refresh the page.
                            </div>
                        `;
                    }
                }

                setCatalogueStatus(
                    "Catalogue unavailable"
                );
            });

    } else {

        setCatalogueStatus(
            `${catalogueBooks.length} books available`
        );

        renderCatalogue();
    }

})();