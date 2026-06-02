import apiClient from "../api/apiClient";

export const getMembers=()=>{

    return apiClient.get(
        "/members"
    );
};

export const createMember=(data)=>{

    return apiClient.post(
        "/members",
        data
    );
};

export const updateMember=(id,data)=>{

    return apiClient.put(
        `/members/${id}`,
        data
    );
};

export const deleteMember=(id)=>{

    return apiClient.delete(
        `/members/${id}`
    );
};