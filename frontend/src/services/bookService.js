import apiClient from "../api/apiClient";

export const getBooks = ()=>{

    return apiClient.get("/books");
};

export const createBook=(data)=>{

    return apiClient.post(
        "/books",
        data
    );
};