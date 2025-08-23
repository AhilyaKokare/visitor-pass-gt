export interface TenantDashboardInfo {
tenantId: number;
tenantName: string;
locationDetails: string;
createdBy: string;
createdAt: string; // The backend sends this as a string in ISO format
adminName: string;
adminEmail: string;
adminContact: string;
adminIsActive: boolean;
}
// --- SUPER ADMIN ANALYTICS DASHBOARD MODELS ---
export interface GlobalStats {
totalTenants: number;
totalUsers: number;
totalPassesIssued: number;
activePassesToday: number;
}
export interface TenantActivity {
tenantId: number;
tenantName: string;
locationDetails: string; // <-- THIS WAS THE MISSING PROPERTY
userCount: number;
passesToday: number;
totalPassesAllTime: number;
}
export interface SuperAdminDashboard {
globalStats: GlobalStats;
tenantActivity: TenantActivity[];
recentPassesAcrossAllTenants: any[];
}