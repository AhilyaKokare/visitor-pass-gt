export interface VisitorPass {
  id: number;
  tenantId: number;
  visitorName: string;
  visitorEmail: string; // <-- ADDED THIS LINE
  visitorPhone: string;
  purpose: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'CHECKED_IN' | 'CHECKED_OUT' | 'EXPIRED';
  passCode: string;
  visitDateTime: string;
  createdByEmployeeName: string;
  approvedBy?: string;
  rejectionReason?: string;
}

export interface CreatePassPayload {
  visitorName: string;
  visitorEmail: string;
  visitorPhone: string;
  purpose: string;
  visitDateTime: string;
}

export interface SecurityPassInfo {
  passId: number;
  visitorName:string;
  passCode: string;
  status: 'APPROVED' | 'CHECKED_IN';
  visitDateTime: string;
  employeeHostName: string;
}