import React, { useEffect, useState } from 'react';
import {
  Table,
  Button,
  Modal,
  Form,
  Select,
  Space,
  Typography,
  message,
  Popconfirm,
  Tag,
  Checkbox,
} from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import api from '../api/axiosInstance';
import type { TransportationDTO, LocationDTO, TransportationType } from '../types';

const { Title } = Typography;

const DAY_LABELS: Record<number, string> = {
  1: 'Mon',
  2: 'Tue',
  3: 'Wed',
  4: 'Thu',
  5: 'Fri',
  6: 'Sat',
  7: 'Sun',
};

const DAY_OPTIONS = Object.entries(DAY_LABELS).map(([value, label]) => ({
  label,
  value: Number(value),
}));

const TYPE_COLORS: Record<TransportationType, string> = {
  FLIGHT: 'blue',
  BUS: 'green',
  SUBWAY: 'orange',
  UBER: 'purple',
};

const TransportationsPage: React.FC = () => {
  const [transportations, setTransportations] = useState<TransportationDTO[]>([]);
  const [locations, setLocations] = useState<LocationDTO[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<TransportationDTO | null>(null);
  const [form] = Form.useForm();

  const fetchTransportations = async () => {
    setLoading(true);
    try {
      const { data } = await api.get<TransportationDTO[]>('/transportations');
      setTransportations(data);
    } catch {
      message.error('Failed to load transportations');
    } finally {
      setLoading(false);
    }
  };

  const fetchLocations = async () => {
    try {
      const { data } = await api.get<LocationDTO[]>('/locations');
      setLocations(data);
    } catch {
      message.error('Failed to load locations');
    }
  };

  useEffect(() => {
    fetchTransportations();
    fetchLocations();
  }, []);

  const openCreateModal = () => {
    setEditing(null);
    form.resetFields();
    setModalOpen(true);
  };

  const openEditModal = (record: TransportationDTO) => {
    setEditing(record);
    form.setFieldsValue({
      originLocationId: record.originLocationId,
      destinationLocationId: record.destinationLocationId,
      transportationType: record.transportationType,
      operatingDays: record.operatingDays,
    });
    setModalOpen(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await api.delete(`/transportations/${id}`);
      message.success('Transportation deleted');
      fetchTransportations();
    } catch {
      message.error('Failed to delete transportation');
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (editing?.id) {
        await api.put(`/transportations/${editing.id}`, values);
        message.success('Transportation updated');
      } else {
        await api.post('/transportations', values);
        message.success('Transportation created');
      }
      setModalOpen(false);
      fetchTransportations();
    } catch {
      // validation or API error
    }
  };

  const getLocationName = (id: number) => {
    const loc = locations.find((l) => l.id === id);
    return loc ? `${loc.name} (${loc.locationCode})` : `ID: ${id}`;
  };

  const columns = [
    {
      title: 'Origin',
      key: 'origin',
      render: (_: unknown, record: TransportationDTO) =>
        record.originLocation
          ? `${record.originLocation.name} (${record.originLocation.locationCode})`
          : getLocationName(record.originLocationId),
    },
    {
      title: 'Destination',
      key: 'destination',
      render: (_: unknown, record: TransportationDTO) =>
        record.destinationLocation
          ? `${record.destinationLocation.name} (${record.destinationLocation.locationCode})`
          : getLocationName(record.destinationLocationId),
    },
    {
      title: 'Type',
      dataIndex: 'transportationType',
      key: 'type',
      render: (type: TransportationType) => (
        <Tag color={TYPE_COLORS[type]}>{type}</Tag>
      ),
    },
    {
      title: 'Operating Days',
      dataIndex: 'operatingDays',
      key: 'operatingDays',
      render: (days: number[]) => (
        <Space size={4}>
          {[1, 2, 3, 4, 5, 6, 7].map((d) => (
            <Tag key={d} color={days.includes(d) ? 'blue' : 'default'}>
              {DAY_LABELS[d]}
            </Tag>
          ))}
        </Space>
      ),
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 150,
      render: (_: unknown, record: TransportationDTO) => (
        <Space>
          <Button type="text" icon={<EditOutlined />} onClick={() => openEditModal(record)} />
          <Popconfirm
            title="Delete this transportation?"
            description="This action cannot be undone."
            onConfirm={() => handleDelete(record.id!)}
            okText="Delete"
            okButtonProps={{ danger: true }}
          >
            <Button type="text" danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={4} style={{ margin: 0 }}>
          Transportations
        </Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={openCreateModal}>
          Add Transportation
        </Button>
      </div>

      <Table
        columns={columns}
        dataSource={transportations}
        rowKey="id"
        loading={loading}
        pagination={{ pageSize: 10 }}
      />

      <Modal
        title={editing ? 'Edit Transportation' : 'Add Transportation'}
        open={modalOpen}
        onOk={handleSubmit}
        onCancel={() => setModalOpen(false)}
        okText={editing ? 'Update' : 'Create'}
        width={520}
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item
            name="originLocationId"
            label="Origin Location"
            rules={[{ required: true, message: 'Origin is required' }]}
          >
            <Select
              showSearch
              placeholder="Select origin"
              optionFilterProp="label"
              options={locations.map((l) => ({
                value: l.id!,
                label: `${l.name} (${l.locationCode})`,
              }))}
            />
          </Form.Item>
          <Form.Item
            name="destinationLocationId"
            label="Destination Location"
            rules={[{ required: true, message: 'Destination is required' }]}
          >
            <Select
              showSearch
              placeholder="Select destination"
              optionFilterProp="label"
              options={locations.map((l) => ({
                value: l.id!,
                label: `${l.name} (${l.locationCode})`,
              }))}
            />
          </Form.Item>
          <Form.Item
            name="transportationType"
            label="Transportation Type"
            rules={[{ required: true, message: 'Type is required' }]}
          >
            <Select
              placeholder="Select type"
              options={[
                { value: 'FLIGHT', label: 'Flight' },
                { value: 'BUS', label: 'Bus' },
                { value: 'SUBWAY', label: 'Subway' },
                { value: 'UBER', label: 'Uber' },
              ]}
            />
          </Form.Item>
          <Form.Item
            name="operatingDays"
            label="Operating Days"
            rules={[{ required: true, message: 'Select at least one day' }]}
          >
            <Checkbox.Group options={DAY_OPTIONS} />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
};

export default TransportationsPage;
