import apiClient from "../api/apiClient";

export const getBooks = () => {

    return apiClient.get("/books");
};

export const createBook = (data) => {

    return apiClient.post(
        "/books",
        data
    );
};

export const deleteBook = (id) => {

    return apiClient.delete(
        `/books/${id}`
    );
};

export const updateBook = (id,data) => {

    return apiClient.put(
       `/books/${id}`,
        data
    );
};