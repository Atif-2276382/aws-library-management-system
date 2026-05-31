import apiClient from "../api/apiClient";

export const getMembers=()=>{

    return apiClient.get("/members");
};

export const createMember=(data)=>{

    return apiClient.post(
        "/members",
        data
    );
};