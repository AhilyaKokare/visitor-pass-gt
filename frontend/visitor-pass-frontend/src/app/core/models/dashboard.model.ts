import { VisitorPass } from './pass.model';

export interface TenantDashboardStats {
  pendingPasses: number;
  approvedPassesToday: number;
  checkedInVisitors: number;
  completedPassesToday: number;
}

export interface PassAuditLog {
  id: number;
  tenantId: number;
  actionDescription: string;
  userId: number;
  passId: number;
  timestamp: string;
}

export interface EmailAuditLog {
  id: number;
  associatedPassId: number;
  recipientAddress: string;
  subject: string;
  status: 'PENDING' | 'SENT' | 'FAILED' | 'DELIVERED' | 'BOUNCED';
  createdAt: string;
  processedAt: string;
  failureReason: string | null;
}

export interface TenantDashboardResponse {
  stats: TenantDashboardStats;
  recentPasses: VisitorPass[];
  recentPassActivity: PassAuditLog[];
  recentEmailActivity: EmailAuditLog[];
}
